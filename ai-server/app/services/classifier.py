from enum import StrEnum
from typing import cast

from pydantic import Field

from app.schemas.interpretation import CategoryCode, SchemaModel


class SafetyFlag(StrEnum):
    SELF_HARM = "self_harm"
    MEDICAL = "medical"
    LEGAL = "legal"
    FINANCIAL = "financial"
    CRIME = "crime"
    PROMPT_INJECTION = "prompt_injection"


class ClassificationResult(SchemaModel):
    category: CategoryCode
    safety_flags: list[SafetyFlag] = Field(default_factory=list)
    requires_safety_response: bool = False


class ConcernClassifier:
    def classify(self, concern: str) -> ClassificationResult:
        normalized = self._normalize(concern)
        safety_flags = self._detect_safety_flags(normalized)

        return ClassificationResult(
            category=self._detect_category(normalized, safety_flags),
            safety_flags=safety_flags,
            requires_safety_response=SafetyFlag.SELF_HARM in safety_flags,
        )

    def _normalize(self, value: str) -> str:
        return " ".join(value.casefold().split())

    def _detect_category(
        self,
        normalized: str,
        safety_flags: list[SafetyFlag],
    ) -> CategoryCode:
        if SafetyFlag.SELF_HARM in safety_flags or SafetyFlag.MEDICAL in safety_flags:
            return "health"

        scores = {
            "love": self._score(normalized, LOVE_KEYWORDS),
            "career": self._score(normalized, CAREER_KEYWORDS),
            "relationship": self._score(normalized, RELATIONSHIP_KEYWORDS),
            "money": self._score(normalized, MONEY_KEYWORDS),
            "health": self._score(normalized, HEALTH_KEYWORDS),
        }
        category, score = max(scores.items(), key=lambda item: item[1])
        if score == 0:
            return "other"
        return cast(CategoryCode, category)

    def _detect_safety_flags(self, normalized: str) -> list[SafetyFlag]:
        flags: list[SafetyFlag] = []
        for flag, keywords in SAFETY_KEYWORDS.items():
            if self._score(normalized, keywords) > 0:
                flags.append(flag)
        return flags

    def _score(self, normalized: str, keywords: tuple[str, ...]) -> int:
        return sum(1 for keyword in keywords if keyword in normalized)


LOVE_KEYWORDS = (
    "연애",
    "연인",
    "사랑",
    "헤어진",
    "재회",
    "이별",
    "결혼",
    "썸",
    "관계",
    "짝사랑",
)

CAREER_KEYWORDS = (
    "직장",
    "회사",
    "이직",
    "퇴사",
    "커리어",
    "진로",
    "취업",
    "면접",
    "상사",
    "동료",
    "일",
)

RELATIONSHIP_KEYWORDS = (
    "친구",
    "가족",
    "부모",
    "인간관계",
    "사람들",
    "갈등",
    "대화",
    "소통",
)

MONEY_KEYWORDS = (
    "돈",
    "금전",
    "투자",
    "주식",
    "코인",
    "대출",
    "빚",
    "월급",
    "수입",
    "사업",
)

HEALTH_KEYWORDS = (
    "건강",
    "병원",
    "진단",
    "치료",
    "약",
    "우울",
    "불안",
    "자해",
    "죽고",
)

SAFETY_KEYWORDS: dict[SafetyFlag, tuple[str, ...]] = {
    SafetyFlag.SELF_HARM: (
        "자해",
        "죽고 싶",
        "죽고싶",
        "극단적",
        "스스로 목숨",
        "자살",
    ),
    SafetyFlag.MEDICAL: (
        "진단",
        "치료",
        "처방",
        "약을 먹",
        "수술",
        "병원",
    ),
    SafetyFlag.LEGAL: (
        "법률",
        "소송",
        "고소",
        "고발",
        "변호사",
        "계약서",
    ),
    SafetyFlag.FINANCIAL: (
        "투자",
        "주식",
        "코인",
        "대출",
        "수익률",
        "매수",
        "매도",
    ),
    SafetyFlag.CRIME: (
        "범죄",
        "증거를 숨",
        "훔치",
        "사기",
        "불법",
        "협박",
    ),
    SafetyFlag.PROMPT_INJECTION: (
        "이전 지시",
        "시스템 규칙",
        "규칙을 바꿔",
        "지시를 무시",
        "프롬프트",
    ),
}
