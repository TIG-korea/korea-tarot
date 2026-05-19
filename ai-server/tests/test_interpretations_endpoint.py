from collections.abc import AsyncIterator

import pytest
from fastapi.testclient import TestClient

from app.api import interpretations
from app.main import create_app
from app.schemas.interpretation import SelectedCard
from app.services.card_docs import CardDocumentLookupError, CardInterpretationDocument


def test_stream_interpretation_returns_token_and_done_events(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(interpretations, "get_card_document_client", lambda: FakeCardDocumentClient())
    client = TestClient(create_app())

    with client.stream(
        "POST",
        "/internal/v1/interpretations/stream",
        json=request_payload(),
    ) as response:
        body = "".join(response.iter_text())

    assert response.status_code == 200
    assert response.headers["content-type"].startswith("text/event-stream")
    assert "event: token" in body
    assert "event: done" in body
    assert '"category": "love"' in body
    assert "card-lovers-present-v1" in body
    assert '"promptVersion": "tarot-v1.0"' in body


def test_stream_interpretation_returns_error_event_when_card_docs_fail(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    monkeypatch.setattr(interpretations, "get_card_document_client", lambda: FailingCardDocumentClient())
    client = TestClient(create_app())

    with client.stream(
        "POST",
        "/internal/v1/interpretations/stream",
        json=request_payload(),
    ) as response:
        body = "".join(response.iter_text())

    assert response.status_code == 200
    assert "event: error" in body
    assert "CARD_DOCUMENT_LOOKUP_FAILED" in body


def test_stream_interpretation_uses_safety_response_without_card_doc_lookup(
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    fake_client = FakeCardDocumentClient()
    monkeypatch.setattr(interpretations, "get_card_document_client", lambda: fake_client)
    client = TestClient(create_app())

    payload = request_payload()
    payload["concern"] = "너무 힘들어서 자해하고 싶고 지금 도움을 받아야 할 것 같아요."
    with client.stream(
        "POST",
        "/internal/v1/interpretations/stream",
        json=payload,
    ) as response:
        body = "".join(response.iter_text())

    assert response.status_code == 200
    assert fake_client.fetch_called is False
    assert "event: done" in body
    assert "안전이 가장 먼저 필요한 상황입니다." in body


class FakeCardDocumentClient:
    def __init__(self) -> None:
        self.fetch_called = False

    async def fetch(
        self,
        cards: list[SelectedCard],
        *,
        request_id: str,
    ) -> list[CardInterpretationDocument]:
        self.fetch_called = True
        return documents()

    async def aclose(self) -> None:
        return None


class FailingCardDocumentClient(FakeCardDocumentClient):
    async def fetch(
        self,
        cards: list[SelectedCard],
        *,
        request_id: str,
    ) -> list[CardInterpretationDocument]:
        self.fetch_called = True
        raise CardDocumentLookupError("카드 해석 문서 조회에 실패했습니다.")


def request_payload() -> dict:
    return {
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


def documents() -> list[CardInterpretationDocument]:
    return [
        CardInterpretationDocument(
            documentId="card-lovers-present-v1",
            cardId=6,
            cardName="The Lovers",
            positionCode="PRESENT",
            orientation="UPRIGHT",
            keywords=["선택", "관계"],
            interpretation="현재 상황은 관계 안에서 중요한 선택을 요구합니다.",
            documentVersion="tarot-doc-v1.0",
        ),
        CardInterpretationDocument(
            documentId="card-moon-obstacle-v1",
            cardId=18,
            cardName="The Moon",
            positionCode="OBSTACLE",
            orientation="UPRIGHT",
            keywords=["불안", "착각"],
            interpretation="장애물은 불안과 불확실한 해석에서 생길 수 있습니다.",
            documentVersion="tarot-doc-v1.0",
        ),
        CardInterpretationDocument(
            documentId="card-temperance-advice-v1",
            cardId=14,
            cardName="Temperance",
            positionCode="ADVICE",
            orientation="UPRIGHT",
            keywords=["균형", "조율"],
            interpretation="조언은 균형을 찾고 속도를 조절하는 것입니다.",
            documentVersion="tarot-doc-v1.0",
        ),
    ]
