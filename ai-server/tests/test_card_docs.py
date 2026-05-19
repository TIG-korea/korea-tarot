import json

import httpx
import pytest

from app.schemas.interpretation import SelectedCard
from app.services.card_docs import CardDocumentLookupError, CardInterpretationClient


@pytest.mark.asyncio
async def test_fetch_card_documents_accepts_api_response_wrapper() -> None:
    async def handler(request: httpx.Request) -> httpx.Response:
        assert request.url.path == "/internal/v1/card-interpretations/lookup"
        assert request.headers["x-request-id"] == "req-abc-123"
        assert json.loads(request.content) == {
            "cards": [
                {"cardId": 6, "positionCode": "PRESENT", "orientation": "UPRIGHT"},
                {"cardId": 18, "positionCode": "OBSTACLE", "orientation": "UPRIGHT"},
                {"cardId": 14, "positionCode": "ADVICE", "orientation": "UPRIGHT"},
            ]
        }
        return httpx.Response(
            200,
            json={
                "success": True,
                "data": {
                    "documents": [
                        {
                            "documentId": "card-lovers-present-v1",
                            "cardId": 6,
                            "cardName": "The Lovers",
                            "positionCode": "PRESENT",
                            "orientation": "UPRIGHT",
                            "keywords": ["선택", "관계"],
                            "interpretation": "현재 상황에 대한 카드 해석입니다.",
                            "documentVersion": "tarot-doc-v1.0",
                        },
                        {
                            "documentId": "card-moon-obstacle-v1",
                            "cardId": 18,
                            "cardName": "The Moon",
                            "positionCode": "OBSTACLE",
                            "orientation": "UPRIGHT",
                            "keywords": ["불안", "착각"],
                            "interpretation": "장애물에 대한 카드 해석입니다.",
                            "documentVersion": "tarot-doc-v1.0",
                        },
                        {
                            "documentId": "card-temperance-advice-v1",
                            "cardId": 14,
                            "cardName": "Temperance",
                            "positionCode": "ADVICE",
                            "orientation": "UPRIGHT",
                            "keywords": ["균형", "조율"],
                            "interpretation": "조언에 대한 카드 해석입니다.",
                            "documentVersion": "tarot-doc-v1.0",
                        },
                    ]
                },
                "error": None,
            },
        )

    async with httpx.AsyncClient(
        transport=httpx.MockTransport(handler),
        base_url="http://backend",
    ) as http_client:
        client = CardInterpretationClient(http_client=http_client)
        documents = await client.fetch(cards(), request_id="req-abc-123")

    assert [document.document_id for document in documents] == [
        "card-lovers-present-v1",
        "card-moon-obstacle-v1",
        "card-temperance-advice-v1",
    ]
    assert documents[0].model_dump(by_alias=True)["documentId"] == "card-lovers-present-v1"


@pytest.mark.asyncio
async def test_fetch_card_documents_rejects_incomplete_documents() -> None:
    async def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(
            200,
            json={
                "documents": [
                    {
                        "documentId": "card-lovers-present-v1",
                        "cardId": 6,
                        "cardName": "The Lovers",
                        "positionCode": "PRESENT",
                        "orientation": "UPRIGHT",
                        "keywords": ["선택"],
                        "interpretation": "현재 상황에 대한 카드 해석입니다.",
                        "documentVersion": "tarot-doc-v1.0",
                    }
                ]
            },
        )

    async with httpx.AsyncClient(
        transport=httpx.MockTransport(handler),
        base_url="http://backend",
    ) as http_client:
        client = CardInterpretationClient(http_client=http_client)

        with pytest.raises(CardDocumentLookupError):
            await client.fetch(cards(), request_id="req-abc-123")


@pytest.mark.asyncio
async def test_fetch_card_documents_converts_backend_error() -> None:
    async def handler(_: httpx.Request) -> httpx.Response:
        return httpx.Response(500, json={"title": "서버 오류"})

    async with httpx.AsyncClient(
        transport=httpx.MockTransport(handler),
        base_url="http://backend",
    ) as http_client:
        client = CardInterpretationClient(http_client=http_client)

        with pytest.raises(CardDocumentLookupError, match="카드 해석 문서 조회에 실패했습니다."):
            await client.fetch(cards(), request_id="req-abc-123")


def cards() -> list[SelectedCard]:
    return [
        SelectedCard(
            cardId=6,
            cardName="The Lovers",
            positionCode="PRESENT",
            orientation="UPRIGHT",
        ),
        SelectedCard(
            cardId=18,
            cardName="The Moon",
            positionCode="OBSTACLE",
            orientation="UPRIGHT",
        ),
        SelectedCard(
            cardId=14,
            cardName="Temperance",
            positionCode="ADVICE",
            orientation="UPRIGHT",
        ),
    ]
