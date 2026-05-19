from app.schemas.interpretation import SelectedCard
from app.services.classifier import ConcernClassifier
from app.services.safety import SafetyResponseService


def test_safety_response_is_required_for_self_harm_signal() -> None:
    classification = ConcernClassifier().classify("너무 힘들어서 자해하고 싶고 지금 도움이 필요해요.")

    assert SafetyResponseService().requires_safety_response(classification) is True


def test_safety_result_uses_urgent_help_guidance_instead_of_prediction() -> None:
    service = SafetyResponseService()
    documents = service.build_documents(cards())
    result = service.build_result(documents)

    assert result.summary == "안전이 가장 먼저 필요한 상황입니다."
    assert "즉시 도움" in result.overall
    assert "예언" not in result.advice
    assert [card.position_code for card in result.cards] == ["PRESENT", "OBSTACLE", "ADVICE"]


def cards() -> list[SelectedCard]:
    return [
        SelectedCard(cardId=6, cardName="The Lovers", positionCode="PRESENT", orientation="UPRIGHT"),
        SelectedCard(cardId=18, cardName="The Moon", positionCode="OBSTACLE", orientation="UPRIGHT"),
        SelectedCard(cardId=14, cardName="Temperance", positionCode="ADVICE", orientation="UPRIGHT"),
    ]
