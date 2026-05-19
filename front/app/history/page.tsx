"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import { getConsultationHistory, type ConsultationHistoryItem } from "@/api/history";

export default function HistoryPage() {
  const [items, setItems] = useState<ConsultationHistoryItem[]>([]);
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadHistory();
  }, []);

  async function loadHistory(cursor?: string) {
    setIsLoading(true);
    setError(null);
    try {
      const response = await getConsultationHistory(cursor);
      setItems((current) => (cursor ? [...current, ...response.items] : response.items));
      setNextCursor(response.nextCursor);
    } catch {
      setError("상담 기록을 불러오지 못했습니다.");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <main className="min-h-screen bg-background bg-mystic-radial px-5 py-8 text-on-surface">
      <section className="mx-auto max-w-5xl">
        <header className="mb-8 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="font-script text-3xl text-primary">Past Readings</p>
            <h1 className="mt-2 font-display text-4xl font-semibold md:text-5xl">상담 기록</h1>
          </div>
          <Link className="gold-button rounded-xl px-6 py-3 text-center font-semibold" href="/consultations/new">
            새 상담 시작
          </Link>
        </header>

        {error && (
          <p className="mb-6 rounded-xl border border-red-300/25 bg-red-950/30 px-4 py-3 text-sm text-red-100">
            {error}
          </p>
        )}

        {!isLoading && items.length === 0 && (
          <section className="vellum-panel rounded-3xl p-8 text-center">
            <p className="text-lg text-on-surface-muted">아직 저장된 상담 기록이 없습니다.</p>
          </section>
        )}

        <div className="space-y-4">
          {items.map((item) => (
            <Link
              className="vellum-panel block rounded-2xl p-5 transition hover:border-primary/70"
              href={`/history/${item.consultationId}`}
              key={item.consultationId}
            >
              <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                <div>
                  <p className="text-sm text-on-surface-muted">{formatDate(item.createdAt)} · {statusLabel(item.status)}</p>
                  <h2 className="mt-2 text-xl font-semibold">{item.summary ?? "해석 결과 생성 전입니다."}</h2>
                  <p className="mt-3 leading-7 text-on-surface-muted">{item.concernPreview}</p>
                </div>
                <div className="flex flex-wrap gap-2 md:justify-end">
                  {item.cardNames.map((cardName) => (
                    <span className="rounded-full border border-outline/30 px-3 py-1 text-sm text-on-surface-muted" key={cardName}>
                      {cardName}
                    </span>
                  ))}
                </div>
              </div>
            </Link>
          ))}
        </div>

        {isLoading && <p className="mt-6 text-center text-on-surface-muted">상담 기록을 불러오고 있습니다...</p>}

        {nextCursor && !isLoading && (
          <div className="mt-8 text-center">
            <button
              className="rounded-xl border border-outline/30 px-6 py-3 text-on-surface-muted transition hover:border-primary hover:text-primary"
              onClick={() => loadHistory(nextCursor)}
              type="button"
            >
              더 보기
            </button>
          </div>
        )}
      </section>
    </main>
  );
}

function statusLabel(status: ConsultationHistoryItem["status"]) {
  if (status === "COMPLETED") {
    return "완료";
  }
  if (status === "FAILED") {
    return "실패";
  }
  if (status === "STREAMING") {
    return "생성 중";
  }
  if (status === "CANCELLED") {
    return "취소";
  }
  return "대기 중";
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}
