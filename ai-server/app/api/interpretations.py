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
from app.services.safety import SafetyResponseService


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
    safety_service = get_safety_service()
    classification = classifier.classify(request.concern)
    client = get_card_document_client()

    try:
        card_documents = await load_card_documents(request, classification, client, safety_service)
        build_tarot_prompt(
            request=request,
            classification=classification,
            card_documents=card_documents,
        )

        for token in build_mock_tokens(classification, safety_service):
            yield format_sse("token", {"text": token})

        payload = build_final_payload(
            request=request,
            classification=classification,
            card_documents=card_documents,
            safety_service=safety_service,
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


def get_safety_service() -> SafetyResponseService:
    return SafetyResponseService()


async def load_card_documents(
    request: StreamInterpretationRequest,
    classification: ClassificationResult,
    client: CardInterpretationClient,
    safety_service: SafetyResponseService,
) -> list[CardInterpretationDocument]:
    if safety_service.requires_safety_response(classification):
        return safety_service.build_documents(request.cards)
    return await client.fetch(request.cards, request_id=request.request_id)


def build_mock_tokens(
    classification: ClassificationResult,
    safety_service: SafetyResponseService,
) -> list[str]:
    if safety_service.requires_safety_response(classification):
        return safety_service.build_tokens()
    return [
        "선택한 세 장의 카드는 고민의 현재 흐름과 막힌 지점,",
        " 그리고 현실적으로 취할 수 있는 다음 행동을 함께 보여줍니다.",
    ]


def build_final_payload(
    *,
    request: StreamInterpretationRequest,
    classification: ClassificationResult,
    card_documents: list[CardInterpretationDocument],
    safety_service: SafetyResponseService,
    latency_ms: int,
) -> InterpretationFinalPayload:
    result = build_result(classification, card_documents, safety_service)
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
    safety_service: SafetyResponseService,
) -> InterpretationResult:
    if safety_service.requires_safety_response(classification):
        return safety_service.build_result(card_documents)

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


def resolve_document_version(card_documents: list[CardInterpretationDocument]) -> str:
    if not card_documents:
        return "unknown"
    return card_documents[0].document_version


def format_sse(event: str, data: dict) -> str:
    payload = json.dumps(data, ensure_ascii=False)
    return f"event: {event}\ndata: {payload}\n\n"
