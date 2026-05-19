from typing import Literal

from pydantic import BaseModel, ConfigDict, Field, PositiveInt, model_validator


PositionCode = Literal["PRESENT", "OBSTACLE", "ADVICE"]
Orientation = Literal["UPRIGHT"]
SpreadType = Literal["THREE_CARD"]
CategoryCode = Literal["love", "career", "relationship", "money", "health", "other"]
PayloadStatus = Literal["success", "failed"]

REQUIRED_POSITIONS = {"PRESENT", "OBSTACLE", "ADVICE"}


def to_camel(value: str) -> str:
    parts = value.split("_")
    return parts[0] + "".join(part.capitalize() for part in parts[1:])


class SchemaModel(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        extra="forbid",
        str_strip_whitespace=True,
        protected_namespaces=(),
    )


class SelectedCard(SchemaModel):
    card_id: PositiveInt
    card_name: str = Field(min_length=1, max_length=100)
    position_code: PositionCode
    orientation: Orientation


class StreamInterpretationRequest(SchemaModel):
    request_id: str = Field(min_length=1, max_length=100)
    consultation_id: PositiveInt
    user_id: PositiveInt
    concern: str = Field(min_length=10, max_length=1000)
    spread_type: SpreadType
    cards: list[SelectedCard] = Field(min_length=3, max_length=3)
    locale: Literal["ko"] = "ko"

    @model_validator(mode="after")
    def validate_positions(self) -> "StreamInterpretationRequest":
        positions = [card.position_code for card in self.cards]
        if set(positions) != REQUIRED_POSITIONS:
            raise ValueError("cards must include PRESENT, OBSTACLE, and ADVICE exactly once")
        return self


class InterpretationCard(SchemaModel):
    position_code: PositionCode
    position_name: str = Field(min_length=1, max_length=50)
    card_id: PositiveInt
    card_name: str = Field(min_length=1, max_length=100)
    interpretation: str = Field(min_length=1)


class InterpretationResult(SchemaModel):
    summary: str = Field(min_length=1, max_length=300)
    overall: str = Field(min_length=1)
    cards: list[InterpretationCard] = Field(min_length=3, max_length=3)
    advice: str = Field(min_length=1)
    caution: str = Field(min_length=1)

    @model_validator(mode="after")
    def validate_card_positions(self) -> "InterpretationResult":
        positions = [card.position_code for card in self.cards]
        if set(positions) != REQUIRED_POSITIONS:
            raise ValueError("result cards must include PRESENT, OBSTACLE, and ADVICE exactly once")
        return self


class TokenUsage(SchemaModel):
    input_tokens: int = Field(ge=0)
    output_tokens: int = Field(ge=0)
    latency_ms: int = Field(ge=0)


class InterpretationFinalPayload(SchemaModel):
    request_id: str = Field(min_length=1, max_length=100)
    status: PayloadStatus
    category: CategoryCode
    model_name: str = Field(min_length=1, max_length=100)
    model_provider: str = Field(min_length=1, max_length=50)
    prompt_version: str = Field(min_length=1, max_length=50)
    document_version: str = Field(min_length=1, max_length=50)
    retrieved_doc_ids: list[str] = Field(default_factory=list)
    result: InterpretationResult
    usage: TokenUsage
