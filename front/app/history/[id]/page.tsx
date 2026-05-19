"use client";

import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";

import { deleteHistory, getHistoryDetail } from "@/api/history";
import type { ConsultationDetailResponse } from "@/api/consultations";
import { ResultView } from "@/features/consultations/ResultView";

export default function HistoryDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const consultationId = useMemo(() => Number(params.id), [params.id]);
  const [detail, setDetail] = useState<ConsultationDetailResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    if (!Number.isFinite(consultationId)) {
      setError("상담 번호가 올바르지 않습니다.");
      setIsLoading(false);
      return;
    }

    getHistoryDetail(consultationId)
      .then(setDetail)
      .catch(() => setError("상담 기록 상세를 불러오지 못했습니다."))
      .finally(() => setIsLoading(false));
  }, [consultationId]);

  async function handleDelete() {
    setIsDeleting(true);
    setError(null);
    try {
      await deleteHistory(consultationId);
      router.push("/history");
    } catch {
      setError("상담 기록 삭제에 실패했습니다.");
      setIsDeleting(false);
    }
  }

  return (
    <main className="min-h-screen bg-background bg-mystic-radial px-5 py-8 text-on-surface">
      <section className="mx-auto max-w-5xl">
        <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <Link className="text-sm text-on-surface-muted transition hover:text-primary" href="/history">
            기록 목록으로
          </Link>
          {detail && (
            <button
              className="rounded-xl border border-red-300/30 px-5 py-2 text-sm text-red-100 transition hover:border-red-200"
              disabled={isDeleting}
              onClick={handleDelete}
              type="button"
            >
              {isDeleting ? "삭제 중..." : "기록 삭제"}
            </button>
          )}
        </div>

        {isLoading && (
          <section className="vellum-panel rounded-3xl p-8 text-center">
            <p className="text-lg text-on-surface-muted">상담 기록을 불러오고 있습니다...</p>
          </section>
        )}

        {error && (
          <p className="mb-6 rounded-xl border border-red-300/25 bg-red-950/30 px-4 py-3 text-sm text-red-100">
            {error}
          </p>
        )}

        {detail && (
          <section className="space-y-6">
            <article className="vellum-panel rounded-3xl p-6 md:p-8">
              <p className="text-sm text-on-surface-muted">{formatDate(detail.createdAt)} · {detail.status}</p>
              <h1 className="mt-3 font-display text-3xl font-semibold md:text-4xl">상담 질문</h1>
              <p className="mt-4 leading-8 text-on-surface-muted">{detail.concern}</p>
            </article>

            <section className="grid gap-3 md:grid-cols-3">
              {detail.cards.map((card) => (
                <article className="vellum-panel rounded-2xl p-4" key={card.positionCode}>
                  <p className="text-sm text-on-surface-muted">{card.positionName}</p>
                  <h2 className="mt-1 font-display text-2xl font-semibold">{card.cardNameEn}</h2>
                  <p className="mt-2 text-sm text-on-surface-muted">{card.cardNameKo}</p>
                </article>
              ))}
            </section>

            {detail.result ? (
              <ResultView result={detail.result} />
            ) : (
              <section className="vellum-panel rounded-3xl p-8 text-center">
                <p className="text-lg text-on-surface-muted">아직 완료된 해석 결과가 없습니다.</p>
              </section>
            )}
          </section>
        )}
      </section>
    </main>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}
