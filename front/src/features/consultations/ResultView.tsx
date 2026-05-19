import Link from "next/link";

import type { InterpretationResult } from "@/api/consultations";

interface ResultViewProps {
  result: InterpretationResult;
}

export function ResultView({ result }: ResultViewProps) {
  return (
    <section className="space-y-6">
      <div className="vellum-panel rounded-3xl p-6 md:p-8">
        <p className="font-script text-3xl text-primary">Reading Summary</p>
        <h1 className="mt-2 font-display text-3xl font-semibold md:text-4xl">{result.summary}</h1>
      </div>

      <ResultSection title="전체 흐름">{result.overall}</ResultSection>

      <section className="space-y-4">
        <h2 className="font-display text-2xl font-semibold text-primary">카드별 해석</h2>
        <div className="grid gap-4 md:grid-cols-3">
          {result.cards.map((card) => (
            <article className="vellum-panel rounded-2xl p-5" key={card.positionCode}>
              <p className="text-sm text-on-surface-muted">{card.positionName}</p>
              <h3 className="mt-1 font-display text-2xl font-semibold">{card.cardName}</h3>
              <p className="mt-4 leading-7 text-on-surface-muted">{card.interpretation}</p>
            </article>
          ))}
        </div>
      </section>

      <ResultSection title="현실적인 조언">{result.advice}</ResultSection>
      <ResultSection title="주의할 점">{result.caution}</ResultSection>

      <p className="rounded-2xl border border-outline/25 bg-surface/70 px-5 py-4 text-sm leading-7 text-on-surface-muted">
        본 서비스의 타로 해석은 엔터테인먼트 및 자기성찰을 위한 참고 정보입니다. 의료, 법률, 금융, 심리 치료 등 전문적인 판단이 필요한 문제는 관련 전문가와 상담하시기 바랍니다.
      </p>

      <div className="flex flex-col gap-3 sm:flex-row sm:justify-end">
        <Link className="rounded-xl border border-outline/30 px-6 py-3 text-center text-on-surface-muted transition hover:border-primary hover:text-primary" href="/">
          기록 보기
        </Link>
        <Link className="gold-button rounded-xl px-6 py-3 text-center font-semibold" href="/consultations/new">
          새 상담 시작
        </Link>
      </div>
    </section>
  );
}

function ResultSection({ title, children }: { title: string; children: string }) {
  return (
    <section className="vellum-panel rounded-2xl p-5 md:p-6">
      <h2 className="font-display text-2xl font-semibold text-primary">{title}</h2>
      <p className="mt-3 leading-8 text-on-surface-muted">{children}</p>
    </section>
  );
}
