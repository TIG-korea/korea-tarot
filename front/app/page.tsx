import Link from "next/link";

export default function HomePage() {
  return (
    <main className="min-h-screen bg-background text-foreground">
      <section className="mx-auto flex min-h-screen max-w-5xl flex-col justify-center px-6 py-16">
        <p className="text-sm font-medium text-primary">AI Tarot Consultation</p>
        <h1 className="mt-4 text-4xl font-semibold tracking-normal md:text-6xl">
          Korea Tarot
        </h1>
        <p className="mt-6 max-w-2xl text-lg leading-8 text-foreground/75">
          고민을 입력하고 서버가 셔플한 카드 3장을 선택하면, 카드 의미와
          고민 맥락을 바탕으로 자기성찰형 타로 해석을 제공합니다.
        </p>
        <div className="mt-10 flex flex-wrap gap-3">
          <Link className="rounded-md bg-primary px-5 py-3 text-sm font-medium text-primary-foreground" href="/signup">
            시작하기
          </Link>
          <Link className="rounded-md border border-border px-5 py-3 text-sm font-medium" href="/login">
            로그인
          </Link>
        </div>
      </section>
    </main>
  );
}

