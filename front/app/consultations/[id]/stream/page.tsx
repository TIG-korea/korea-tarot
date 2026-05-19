"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";

import {
  type ConsultationMetaEvent,
  type ConsultationStreamErrorEvent,
  type InterpretationResult,
  streamConsultationEvents
} from "@/api/consultations";
import { ResultView } from "@/features/consultations/ResultView";

export default function StreamPage() {
  const params = useParams<{ id: string }>();
  const consultationId = useMemo(() => Number(params.id), [params.id]);
  const [meta, setMeta] = useState<ConsultationMetaEvent | null>(null);
  const [streamingText, setStreamingText] = useState("");
  const [result, setResult] = useState<InterpretationResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!Number.isFinite(consultationId)) {
      setError("상담 번호가 올바르지 않습니다.");
      return;
    }

    let isMounted = true;

    streamConsultationEvents(consultationId, {
      onMeta: (event) => {
        if (isMounted) {
          setMeta(event);
        }
      },
      onToken: (text) => {
        if (isMounted) {
          setStreamingText((current) => `${current}${text}`);
        }
      },
      onDone: (event) => {
        if (isMounted) {
          setResult(event.result);
          sessionStorage.setItem(resultStorageKey(consultationId), JSON.stringify(event.result));
        }
      },
      onError: (event: ConsultationStreamErrorEvent) => {
        if (isMounted) {
          setError(event.message);
        }
      }
    }).catch(() => {
      if (isMounted) {
        setError("상담 스트림 연결에 실패했습니다. 로그인 상태를 확인해주세요.");
      }
    });

    return () => {
      isMounted = false;
    };
  }, [consultationId]);

  return (
    <main className="min-h-screen bg-background bg-mystic-radial px-5 py-8 text-on-surface">
      <section className="mx-auto max-w-5xl">
        <header className="mb-8">
          <p className="font-script text-3xl text-primary">Reading in Progress</p>
          <h1 className="mt-2 font-display text-4xl font-semibold md:text-5xl">상담 해석을 생성하고 있습니다</h1>
          <p className="mt-3 text-lg text-on-surface-muted">상담 번호 {consultationId}</p>
        </header>

        {meta && (
          <section className="mb-6 grid gap-3 md:grid-cols-3">
            {meta.cards.map((card) => (
              <article className="vellum-panel rounded-2xl p-4" key={card.positionCode}>
                <p className="text-sm text-on-surface-muted">{positionLabel(card.positionCode)}</p>
                <h2 className="mt-1 font-display text-2xl font-semibold">{card.cardName}</h2>
              </article>
            ))}
          </section>
        )}

        {!result && !error && (
          <section className="vellum-panel rounded-3xl p-6 md:p-8">
            <p className="text-sm text-on-surface-muted">생성 중</p>
            <p className="mt-4 min-h-32 whitespace-pre-wrap text-xl leading-9">
              {streamingText || "카드의 흐름을 읽고 있습니다..."}
            </p>
          </section>
        )}

        {error && (
          <section className="vellum-panel rounded-3xl p-6 md:p-8">
            <h2 className="font-display text-3xl font-semibold text-primary">해석 생성 실패</h2>
            <p className="mt-4 text-lg leading-8 text-on-surface-muted">{error}</p>
            <Link className="gold-button mt-6 inline-block rounded-xl px-6 py-3 font-semibold" href="/consultations/new">
              새 상담 시작
            </Link>
          </section>
        )}

        {result && (
          <>
            <ResultView result={result} />
            <div className="mt-4 text-right">
              <Link className="text-sm text-on-surface-muted transition hover:text-primary" href={`/consultations/${consultationId}`}>
                상세 화면으로 보기
              </Link>
            </div>
          </>
        )}
      </section>
    </main>
  );
}

function positionLabel(positionCode: string) {
  if (positionCode === "PRESENT") {
    return "현재 상황";
  }
  if (positionCode === "OBSTACLE") {
    return "장애물 또는 숨겨진 원인";
  }
  return "조언 또는 방향";
}

function resultStorageKey(consultationId: number) {
  return `consultation-result-${consultationId}`;
}
