import json
import time
from collections.abc import AsyncIterator

from fastapi import APIRouter
from fastapi.responses import StreamingResponse

from app.prompts.tarot_v1 import PROMPT_VERSION, build_tarot_prompt
from app.schemas.interpretation import (
    InterpretationCard,
    InterpretationFinalPayload,
    InterpretationResult,
    SelectedCard,
    StreamInterpretationRequest,
    TokenUsage,
)
from app.services.card_docs import (
    CardDocumentLookupError,
    CardInterpretationClient,
    CardInterpretationDocument,
)
from app.services.classifier import ClassificationResult, ConcernClassifier


router = APIRouter(prefix="/internal/v1/interpretations", tags=["interpretations"])

POSITION_NAMES = {
    "PRESENT": "현재 상황",
    "OBSTACLE": "장애물 또는 숨겨진 원인",
    "ADVICE": "조언 또는 방향",
}


@router.post("/stream")
async def stream_interpretation(
    request: StreamInterpretationRequest,
) -> StreamingResponse:
    return StreamingResponse(
        generate_interpretation_events(request),
        media_type="text/event-stream",
    )


async def generate_interpretation_events(
    request: StreamInterpretationRequest,
) -> AsyncIterator[str]:
    started_at = time.perf_counter()
    classifier = get_classifier()
    classification = classifier.classify(request.concern)
    client = get_card_document_client()

    try:
        card_documents = await load_card_documents(request, classification, client)
        build_tarot_prompt(
            request=request,
            classification=classification,
            card_documents=card_documents,
        )

        for token in build_mock_tokens(classification):
            yield format_sse("token", {"text": token})

        payload = build_final_payload(
            request=request,
            classification=classification,
            card_documents=card_documents,
            latency_ms=int((time.perf_counter() - started_at) * 1000),
        )
        yield format_sse("done", payload.model_dump(by_alias=True))
    except CardDocumentLookupError:
        yield format_sse(
            "error",
            {
                "code": "CARD_DOCUMENT_LOOKUP_FAILED",
                "message": "카드 해석 문서를 조회하지 못했습니다.",
            },
        )
    finally:
        await client.aclose()


def get_classifier() -> ConcernClassifier:
    return ConcernClassifier()


def get_card_document_client() -> CardInterpretationClient:
    return CardInterpretationClient()


async def load_card_documents(
    request: StreamInterpretationRequest,
    classification: ClassificationResult,
    client: CardInterpretationClient,
) -> list[CardInterpretationDocument]:
    if classification.requires_safety_response:
        return build_safety_documents(request.cards)
    return await client.fetch(request.cards, request_id=request.request_id)


def build_mock_tokens(classification: ClassificationResult) -> list[str]:
    if classification.requires_safety_response:
        return [
            "지금은 타로 해석보다 안전을 먼저 확인해야 합니다.",
            " 혼자 감당하지 말고 즉시 주변 사람이나 긴급 지원 기관에 도움을 요청하세요.",
        ]
    return [
        "선택한 세 장의 카드는 고민의 현재 흐름과 막힌 지점,",
        " 그리고 현실적으로 취할 수 있는 다음 행동을 함께 보여줍니다.",
    ]


def build_final_payload(
    *,
    request: StreamInterpretationRequest,
    classification: ClassificationResult,
    card_documents: list[CardInterpretationDocument],
    latency_ms: int,
) -> InterpretationFinalPayload:
    result = build_result(classification, card_documents)
    return InterpretationFinalPayload(
        request_id=request.request_id,
        status="success",
        category=classification.category,
        model_name="mock-tarot-v1",
        model_provider="mock",
        prompt_version=PROMPT_VERSION,
        document_version=resolve_document_version(card_documents),
        retrieved_doc_ids=[document.document_id for document in card_documents],
        result=result,
        usage=TokenUsage(input_tokens=0, output_tokens=0, latency_ms=latency_ms),
    )


def build_result(
    classification: ClassificationResult,
    card_documents: list[CardInterpretationDocument],
) -> InterpretationResult:
    if classification.requires_safety_response:
        summary = "안전이 가장 먼저 필요한 상황입니다."
        overall = (
            "현재 고민은 타로의 상징으로 단정하기보다 즉시 도움을 받을 수 있는 환경을 "
            "만드는 것이 우선입니다. 가까운 사람, 지역 긴급 지원, 전문기관에 지금 상태를 "
            "알리는 방향이 필요합니다."
        )
        advice = "지금 혼자 있지 말고 신뢰할 수 있는 사람에게 연락하거나 긴급 지원 기관에 도움을 요청하세요."
        caution = "이 결과는 위기 상황을 대신 해결하지 않습니다. 위험이 임박했다면 즉시 긴급 구조 요청을 하세요."
    else:
        summary = "카드가 고민을 정리할 세 가지 실마리를 보여줍니다."
        overall = (
            "현재 상황은 감정과 현실 판단을 함께 살펴야 하는 흐름입니다. 장애물 위치의 "
            "카드는 불안하거나 불분명한 요소를 점검하라고 말하고, 조언 위치의 카드는 "
            "균형 있는 선택을 통해 다음 단계를 정리하라고 제안합니다."
        )
        advice = "바로 결론을 내리기보다 지금 확인 가능한 사실과 감정을 나누어 적고, 작은 행동부터 정하세요."
        caution = "타로 해석은 참고 정보입니다. 의료, 법률, 금융처럼 전문 판단이 필요한 문제는 전문가와 상담하세요."

    return InterpretationResult(
        summary=summary,
        overall=overall,
        cards=[
            InterpretationCard(
                position_code=document.position_code,
                position_name=POSITION_NAMES[document.position_code],
                card_id=document.card_id,
                card_name=document.card_name,
                interpretation=document.interpretation,
            )
            for document in card_documents
        ],
        advice=advice,
        caution=caution,
    )


def build_safety_documents(
    cards: list[SelectedCard],
) -> list[CardInterpretationDocument]:
    return [
        CardInterpretationDocument(
            document_id=f"safety-{card.position_code.lower()}",
            card_id=card.card_id,
            card_name=card.card_name,
            position_code=card.position_code,
            orientation=card.orientation,
            keywords=["안전", "도움 요청"],
            interpretation="이 위치의 카드는 구체적인 예언보다 지금 안전을 확보하는 행동이 우선임을 안내합니다.",
            document_version="safety-v1.0",
        )
        for card in cards
    ]


def resolve_document_version(card_documents: list[CardInterpretationDocument]) -> str:
    if not card_documents:
        return "unknown"
    return card_documents[0].document_version


def format_sse(event: str, data: dict) -> str:
    payload = json.dumps(data, ensure_ascii=False)
    return f"event: {event}\ndata: {payload}\n\n"
