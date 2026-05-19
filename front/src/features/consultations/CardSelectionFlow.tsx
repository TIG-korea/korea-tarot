"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";

import { createConsultation, type CardSelectionRequest, type PositionCode } from "@/api/consultations";

const POSITIONS: Array<{ code: PositionCode; label: string; guide: string }> = [
  { code: "PRESENT", label: "현재 상황", guide: "현재 상황을 의미할 카드를 선택해주세요." },
  { code: "OBSTACLE", label: "장애물", guide: "장애물 또는 숨겨진 원인을 의미할 카드를 선택해주세요." },
  { code: "ADVICE", label: "조언", guide: "조언 또는 방향을 의미할 카드를 선택해주세요." }
];

interface CardSelectionFlowProps {
  draftId?: string;
  deckSize?: number;
  expiresAt?: string;
}

export function CardSelectionFlow({ draftId, deckSize = 22, expiresAt }: CardSelectionFlowProps) {
  const router = useRouter();
  const [selections, setSelections] = useState<CardSelectionRequest[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const currentPosition = POSITIONS[selections.length];
  const selectedIndexes = useMemo(() => new Set(selections.map((selection) => selection.deckIndex)), [selections]);

  function selectCard(deckIndex: number) {
    if (!currentPosition || selectedIndexes.has(deckIndex)) {
      return;
    }
    setSelections((current) => [...current, { deckIndex, positionCode: currentPosition.code }]);
    setError(null);
  }

  function undoSelection() {
    setSelections((current) => current.slice(0, -1));
    setError(null);
  }

  async function submitSelection() {
    if (!draftId) {
      setError("Draft Deck 정보가 없습니다. 고민 입력부터 다시 진행해주세요.");
      return;
    }
    if (selections.length !== 3) {
      setError("정확히 3장의 카드를 선택해야 합니다.");
      return;
    }

    setIsSubmitting(true);
    setError(null);
    try {
      const idempotencyKey = createIdempotencyKey();
      const consultation = await createConsultation({ draftId, selections }, idempotencyKey);
      router.push(`/consultations/${consultation.consultationId}/stream`);
    } catch {
      setError("상담 생성에 실패했습니다. Draft가 만료되었거나 선택값이 올바르지 않을 수 있습니다.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="min-h-screen bg-background bg-mystic-radial px-5 py-8 text-on-surface">
      <section className="mx-auto max-w-6xl">
        <header className="flex flex-col gap-5 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="font-script text-3xl text-primary">Three Card Reading</p>
            <h1 className="mt-2 font-display text-4xl font-semibold md:text-5xl">세 장의 카드를 선택하세요</h1>
            <p className="mt-3 text-lg text-on-surface-muted">
              {currentPosition ? currentPosition.guide : "세 장의 카드가 모두 선택되었습니다."}
            </p>
          </div>
          <div className="rounded-2xl border border-outline/25 bg-surface/70 px-5 py-4 text-sm text-on-surface-muted">
            <p>Draft: {draftId ?? "없음"}</p>
            {expiresAt && <p>만료 시각: {expiresAt}</p>}
          </div>
        </header>

        <div className="mt-8 grid gap-3 sm:grid-cols-3">
          {POSITIONS.map((position, index) => {
            const selection = selections[index];
            return (
              <div className="vellum-panel rounded-2xl p-4" key={position.code}>
                <p className="text-sm text-on-surface-muted">{index + 1}번째 위치</p>
                <p className="mt-1 text-xl font-semibold text-primary">{position.label}</p>
                <p className="mt-2 text-sm text-on-surface-muted">
                  {selection ? `선택한 카드 번호: ${selection.deckIndex + 1}` : "아직 선택되지 않았습니다."}
                </p>
              </div>
            );
          })}
        </div>

        <div className="mt-8 grid grid-cols-4 gap-3 sm:grid-cols-6 md:grid-cols-11">
          {Array.from({ length: deckSize }, (_, index) => {
            const isSelected = selectedIndexes.has(index);
            return (
              <button
                className={[
                  "aspect-[2/3] rounded-xl border text-sm transition-all",
                  "bg-gradient-to-b from-surface-muted to-background",
                  isSelected
                    ? "border-primary text-primary shadow-glow"
                    : "border-outline/25 text-on-surface-muted hover:-translate-y-1 hover:border-primary/70"
                ].join(" ")}
                disabled={isSelected || !currentPosition || isSubmitting}
                key={index}
                onClick={() => selectCard(index)}
                type="button"
              >
                <span className="block font-script text-3xl">✦</span>
                <span>{index + 1}</span>
              </button>
            );
          })}
        </div>

        {error && (
          <p className="mt-6 rounded-xl border border-red-300/25 bg-red-950/30 px-4 py-3 text-sm text-red-100">
            {error}
          </p>
        )}

        <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-end">
          <button
            className="rounded-xl border border-outline/30 px-6 py-3 text-on-surface-muted transition hover:border-primary hover:text-primary"
            disabled={selections.length === 0 || isSubmitting}
            onClick={undoSelection}
            type="button"
          >
            선택 취소
          </button>
          <button
            className="gold-button rounded-xl px-8 py-3 font-semibold"
            disabled={selections.length !== 3 || isSubmitting}
            onClick={submitSelection}
            type="button"
          >
            {isSubmitting ? "상담을 생성하는 중..." : "해석 받기"}
          </button>
        </div>
      </section>
    </main>
  );
}

function createIdempotencyKey() {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}
