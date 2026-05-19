from app.schemas.interpretation import InterpretationCard, InterpretationResult, SelectedCard
from app.services.card_docs import CardInterpretationDocument
from app.services.classifier import ClassificationResult


class SafetyResponseService:
    def requires_safety_response(self, classification: ClassificationResult) -> bool:
        return classification.requires_safety_response

    def build_tokens(self) -> list[str]:
        return [
            "지금은 타로 해석보다 안전을 먼저 확인해야 합니다.",
            " 혼자 감당하지 말고 즉시 주변 사람이나 긴급 지원 기관에 도움을 요청하세요.",
        ]

    def build_documents(self, cards: list[SelectedCard]) -> list[CardInterpretationDocument]:
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

    def build_result(self, card_documents: list[CardInterpretationDocument]) -> InterpretationResult:
        return InterpretationResult(
            summary="안전이 가장 먼저 필요한 상황입니다.",
            overall=(
                "현재 고민은 타로의 상징으로 단정하기보다 즉시 도움을 받을 수 있는 환경을 "
                "만드는 것이 우선입니다. 가까운 사람, 지역 긴급 지원, 전문기관에 지금 상태를 "
                "알리는 방향이 필요합니다."
            ),
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
            advice="지금 혼자 있지 말고 신뢰할 수 있는 사람에게 연락하거나 긴급 지원 기관에 도움을 요청하세요.",
            caution="이 결과는 위기 상황을 대신 해결하지 않습니다. 위험이 임박했다면 즉시 긴급 구조 요청을 하세요.",
        )


POSITION_NAMES = {
    "PRESENT": "현재 상황",
    "OBSTACLE": "장애물 또는 숨겨진 원인",
    "ADVICE": "조언 또는 방향",
}
