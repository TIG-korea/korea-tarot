"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

export default function HomePage() {
  const router = useRouter();
  const [concern, setConcern] = useState("");

  function handleStart(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const params = new URLSearchParams();
    if (concern.trim()) {
      params.set("concern", concern.trim());
    }
    router.push(`/consultations/new${params.size > 0 ? `?${params.toString()}` : ""}`);
  }

  return (
    <main className="mystic-bg min-h-screen text-on-surface">
      <header className="mx-auto flex max-w-6xl items-center justify-between px-5 py-6">
        <Link className="font-script text-5xl text-primary" href="/">
          Aetheria
        </Link>
        <nav className="hidden items-center gap-7 text-sm text-on-surface-muted md:flex">
          <Link className="text-primary" href="/">
            오라클
          </Link>
          <Link className="transition hover:text-primary" href="/consultations/new">
            타로 뽑기
          </Link>
          <Link className="transition hover:text-primary" href="/history">
            상담 기록
          </Link>
        </nav>
        <Link className="rounded-full border border-outline/30 px-5 py-2 text-sm text-on-surface-muted transition hover:border-primary hover:text-primary" href="/login">
          로그인
        </Link>
      </header>

      <section className="mx-auto flex min-h-[calc(100vh-6rem)] max-w-5xl flex-col items-center justify-center px-5 pb-20 text-center">
        <p className="font-script text-4xl text-secondary">The stars know your destiny</p>
        <h1 className="mt-4 font-display text-5xl font-semibold leading-tight drop-shadow md:text-7xl">
          별들은 당신의 운명을
          <br />
          알고 있습니다.
        </h1>
        <p className="mt-6 max-w-2xl text-lg leading-8 text-on-surface-muted">
          마음속 질문을 적고, 서버가 셔플한 카드에서 세 장을 선택해 자기성찰형 타로 해석을 받아보세요.
        </p>

        <form className="vellum-panel mt-10 flex w-full max-w-2xl flex-col gap-3 rounded-2xl p-3 md:flex-row" onSubmit={handleStart}>
          <input
            className="mystic-input flex-1 rounded-xl px-4 py-3 text-lg"
            onChange={(event) => setConcern(event.target.value)}
            placeholder="무엇이 당신의 마음을 무겁게 하나요?"
            type="text"
            value={concern}
          />
          <button className="gold-button rounded-xl px-8 py-3 font-semibold" type="submit">
            상담 시작하기
          </button>
        </form>
      </section>
    </main>
  );
}
