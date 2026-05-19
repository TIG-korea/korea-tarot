from typing import Any

import httpx
from pydantic import Field, PositiveInt, model_validator

from app.core.config import settings
from app.schemas.interpretation import (
    Orientation,
    PositionCode,
    REQUIRED_POSITIONS,
    SchemaModel,
    SelectedCard,
)


class CardDocumentLookupError(RuntimeError):
    pass


class CardInterpretationDocument(SchemaModel):
    document_id: str = Field(min_length=1, max_length=100)
    card_id: PositiveInt
    card_name: str = Field(min_length=1, max_length=100)
    position_code: PositionCode
    orientation: Orientation
    keywords: list[str] = Field(default_factory=list)
    interpretation: str = Field(min_length=1)
    document_version: str = Field(min_length=1, max_length=50)


class CardDocumentLookupResponse(SchemaModel):
    documents: list[CardInterpretationDocument] = Field(min_length=3, max_length=3)

    @model_validator(mode="after")
    def validate_positions(self) -> "CardDocumentLookupResponse":
        positions = [document.position_code for document in self.documents]
        if set(positions) != REQUIRED_POSITIONS:
            raise ValueError("documents must include PRESENT, OBSTACLE, and ADVICE exactly once")
        return self


class CardInterpretationClient:
    def __init__(
        self,
        *,
        http_client: httpx.AsyncClient | None = None,
        base_url: str | None = None,
        path: str | None = None,
        timeout_seconds: float | None = None,
    ) -> None:
        self._owns_client = http_client is None
        self._path = path or settings.backend_card_docs_path
        self._client = http_client or httpx.AsyncClient(
            base_url=base_url or settings.backend_base_url,
            timeout=timeout_seconds or settings.backend_timeout_seconds,
        )

    async def fetch(
        self,
        cards: list[SelectedCard],
        *,
        request_id: str,
    ) -> list[CardInterpretationDocument]:
        try:
            response = await self._client.post(
                self._path,
                json={"cards": [self._to_lookup_item(card) for card in cards]},
                headers={"X-Request-Id": request_id},
            )
            response.raise_for_status()
            payload = self._unwrap_api_response(response.json())
            return CardDocumentLookupResponse.model_validate(payload).documents
        except (httpx.HTTPError, ValueError) as exc:
            raise CardDocumentLookupError("카드 해석 문서 조회에 실패했습니다.") from exc

    async def aclose(self) -> None:
        if self._owns_client:
            await self._client.aclose()

    def _to_lookup_item(self, card: SelectedCard) -> dict[str, Any]:
        return {
            "cardId": card.card_id,
            "positionCode": card.position_code,
            "orientation": card.orientation,
        }

    def _unwrap_api_response(self, payload: dict[str, Any]) -> dict[str, Any]:
        if "data" in payload and "success" in payload:
            data = payload.get("data")
            if not isinstance(data, dict):
                raise ValueError("ApiResponse data must be an object")
            return data
        return payload
