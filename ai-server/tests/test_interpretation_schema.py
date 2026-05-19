import pytest
from pydantic import ValidationError

from app.schemas.interpretation import (
    InterpretationFinalPayload,
    InterpretationResult,
    StreamInterpretationRequest,
)


def test_stream_request_accepts_contract_payload() -> None:
    request = StreamInterpretationRequest.model_validate(
        {
            "requestId": "req-abc-123",
            "consultationId": 1001,
            "userId": 1,
            "concern": "현재 만나는 사람과 관계를 계속 이어가도 될지 고민돼요.",
            "spreadType": "THREE_CARD",
            "cards": [
                {
                    "cardId": 6,
                    "cardName": "The Lovers",
                    "positionCode": "PRESENT",
                    "orientation": "UPRIGHT",
                },
                {
                    "cardId": 18,
                    "cardName": "The Moon",
                    "positionCode": "OBSTACLE",
                    "orientation": "UPRIGHT",
                },
                {
                    "cardId": 14,
                    "cardName": "Temperance",
                    "positionCode": "ADVICE",
                    "orientation": "UPRIGHT",
                },
            ],
            "locale": "ko",
        }
    )

    assert request.request_id == "req-abc-123"
    assert request.spread_type == "THREE_CARD"
    assert [card.position_code for card in request.cards] == [
        "PRESENT",
        "OBSTACLE",
        "ADVICE",
    ]
    assert request.model_dump(by_alias=True)["requestId"] == "req-abc-123"


def test_stream_request_rejects_duplicate_positions() -> None:
    with pytest.raises(ValidationError):
        StreamInterpretationRequest.model_validate(
            {
                "requestId": "req-abc-123",
                "consultationId": 1001,
                "userId": 1,
                "concern": "현재 만나는 사람과 관계를 계속 이어가도 될지 고민돼요.",
                "spreadType": "THREE_CARD",
                "cards": [
                    {
                        "cardId": 6,
                        "cardName": "The Lovers",
                        "positionCode": "PRESENT",
                        "orientation": "UPRIGHT",
                    },
                    {
                        "cardId": 18,
                        "cardName": "The Moon",
                        "positionCode": "PRESENT",
                        "orientation": "UPRIGHT",
                    },
                    {
                        "cardId": 14,
                        "cardName": "Temperance",
                        "positionCode": "ADVICE",
                        "orientation": "UPRIGHT",
                    },
                ],
                "locale": "ko",
            }
        )


def test_final_payload_accepts_required_result_schema() -> None:
    payload = InterpretationFinalPayload.model_validate(
        {
            "requestId": "req-abc-123",
            "status": "success",
            "category": "love",
            "modelName": "gpt-4o-mini",
            "modelProvider": "openai",
            "promptVersion": "tarot-v1.0",
            "documentVersion": "tarot-doc-v1.0",
            "retrievedDocIds": [
                "card-lovers-present-v1",
                "ctx-love-decision-001",
            ],
            "result": {
                "summary": "관계에 대한 중요한 선택의 시기입니다.",
                "overall": "전체 흐름 해석입니다.",
                "cards": [
                    {
                        "positionCode": "PRESENT",
                        "positionName": "현재 상황",
                        "cardId": 6,
                        "cardName": "The Lovers",
                        "interpretation": "현재 상황에 대한 카드 해석입니다.",
                    },
                    {
                        "positionCode": "OBSTACLE",
                        "positionName": "장애물 또는 숨겨진 원인",
                        "cardId": 18,
                        "cardName": "The Moon",
                        "interpretation": "장애물에 대한 카드 해석입니다.",
                    },
                    {
                        "positionCode": "ADVICE",
                        "positionName": "조언 또는 방향",
                        "cardId": 14,
                        "cardName": "Temperance",
                        "interpretation": "조언에 대한 카드 해석입니다.",
                    },
                ],
                "advice": "현실적인 조언입니다.",
                "caution": "주의할 점입니다.",
            },
            "usage": {
                "inputTokens": 2500,
                "outputTokens": 900,
                "latencyMs": 6800,
            },
        }
    )

    assert payload.category == "love"
    assert isinstance(payload.result, InterpretationResult)
    assert payload.model_dump(by_alias=True)["usage"]["latencyMs"] == 6800


def test_result_rejects_missing_required_sections() -> None:
    with pytest.raises(ValidationError):
        InterpretationResult.model_validate(
            {
                "summary": "요약입니다.",
                "overall": "전체 흐름입니다.",
                "cards": [],
                "caution": "주의할 점입니다.",
            }
        )
