"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";

import { getConsultation, type InterpretationResult } from "@/api/consultations";
import { ResultView } from "@/features/consultations/ResultView";

export default function ConsultationDetailPage() {
  const params = useParams<{ id: string }>();
  const consultationId = useMemo(() => Number(params.id), [params.id]);
  const [result, setResult] = useState<InterpretationResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!Number.isFinite(consultationId)) {
      setError("상담 번호가 올바르지 않습니다.");
      setIsLoading(false);
      return;
    }

    const cached = sessionStorage.getItem(resultStorageKey(consultationId));
    if (cached) {
      setResult(JSON.parse(cached) as InterpretationResult);
      setIsLoading(false);
      return;
    }

    getConsultation(consultationId)
      .then((detail) => {
        setResult(detail.result);
      })
      .catch(() => {
        setError("상담 상세를 불러오지 못했습니다.");
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, [consultationId]);

  return (
    <main className="min-h-screen bg-background bg-mystic-radial px-5 py-8 text-on-surface">
      <section className="mx-auto max-w-5xl">
        {isLoading && (
          <section className="vellum-panel rounded-3xl p-8 text-center">
            <p className="text-lg text-on-surface-muted">상담 결과를 불러오고 있습니다...</p>
          </section>
        )}

        {!isLoading && result && <ResultView result={result} />}

        {!isLoading && !result && (
          <section className="vellum-panel rounded-3xl p-8 text-center">
            <p className="font-script text-3xl text-primary">No Reading</p>
            <h1 className="mt-2 font-display text-3xl font-semibold">상담 결과를 표시할 수 없습니다</h1>
            <p className="mt-4 text-lg text-on-surface-muted">{error ?? "완료된 상담 결과가 없습니다."}</p>
            <Link className="gold-button mt-6 inline-block rounded-xl px-6 py-3 font-semibold" href="/consultations/new">
              새 상담 시작
            </Link>
          </section>
        )}
      </section>
    </main>
  );
}

function resultStorageKey(consultationId: number) {
  return `consultation-result-${consultationId}`;
}
