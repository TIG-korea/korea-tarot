"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";

import { createDraft } from "@/api/consultations";
import { validateConcern } from "@/utils/validators";

interface NewConsultationFormProps {
  initialConcern?: string;
}

export function NewConsultationForm({ initialConcern = "" }: NewConsultationFormProps) {
  const router = useRouter();
  const [concern, setConcern] = useState(initialConcern);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validationError = validateConcern(concern);
  const canSubmit = !validationError && !isSubmitting;

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const nextError = validateConcern(concern);
    if (nextError) {
      setError(nextError);
      return;
    }

    setError(null);
    setIsSubmitting(true);
    try {
      const draft = await createDraft({ concern: concern.trim() });
      const params = new URLSearchParams({
        draftId: draft.draftId,
        deckSize: String(draft.deckSize),
        expiresAt: draft.expiresAt
      });
      router.push(`/consultations/select?${params.toString()}`);
    } catch {
      setError("Draft Deck 생성에 실패했습니다. 로그인 상태와 입력 내용을 확인해주세요.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="mystic-bg min-h-screen px-5 py-8 text-on-surface">
      <section className="mx-auto flex min-h-[calc(100vh-4rem)] max-w-3xl items-center justify-center">
        <form className="vellum-panel w-full rounded-3xl p-6 md:p-10" onSubmit={handleSubmit}>
          <p className="font-script text-3xl text-primary">Open the Veil</p>
          <h1 className="mt-3 font-display text-4xl font-semibold leading-tight md:text-5xl">
            지금 마음에 걸린 질문을 적어주세요
          </h1>
          <p className="mt-4 text-lg leading-8 text-on-surface-muted">
            질문은 길지 않아도 괜찮습니다. 상황과 마음을 함께 적으면 카드 해석이 더 선명해집니다.
          </p>

          <div className="mt-8">
            <textarea
              className="mystic-input min-h-56 resize-none rounded-2xl px-5 py-4 text-lg leading-8"
              maxLength={1000}
              onChange={(event) => setConcern(event.target.value)}
              placeholder="요즘 직장을 계속 다녀야 할지, 이직을 준비해야 할지 고민입니다."
              value={concern}
            />
            <div className="mt-3 flex flex-col gap-2 text-sm text-on-surface-muted md:flex-row md:items-center md:justify-between">
              <span>상담 내용에는 이름, 연락처, 주소, 주민등록번호 등 개인 식별 정보를 입력하지 않는 것을 권장합니다.</span>
              <span>{concern.length} / 1000</span>
            </div>
          </div>

          {(error || validationError) && (
            <p className="mt-4 rounded-xl border border-red-300/25 bg-red-950/30 px-4 py-3 text-sm text-red-100">
              {error ?? validationError}
            </p>
          )}

          <button className="gold-button mt-8 w-full rounded-xl px-6 py-4 text-base font-semibold" disabled={!canSubmit} type="submit">
            {isSubmitting ? "별의 배열을 준비하는 중..." : "카드 펼치기"}
          </button>
        </form>
      </section>
    </main>
  );
}
