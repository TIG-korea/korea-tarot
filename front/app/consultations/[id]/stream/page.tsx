import Link from "next/link";

type Params = Promise<{ id: string }>;

interface StreamPageProps {
  params: Params;
}

export default async function StreamPage({ params }: StreamPageProps) {
  const { id } = await params;

  return (
    <main className="mystic-bg flex min-h-screen items-center justify-center px-5 py-10 text-on-surface">
      <section className="vellum-panel w-full max-w-2xl rounded-3xl p-8 text-center md:p-10">
        <p className="font-script text-4xl text-primary">Reading Prepared</p>
        <h1 className="mt-3 font-display text-4xl font-semibold">상담이 생성되었습니다</h1>
        <p className="mt-5 text-lg leading-8 text-on-surface-muted">
          상담 번호 {id}의 결과 스트리밍 화면입니다. SSE 백엔드가 연결되면 이 화면에서 진행 상태와 최종 해석을 표시합니다.
        </p>
        <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
          <Link className="gold-button rounded-xl px-6 py-3 font-semibold" href="/consultations/new">
            새 상담 시작
          </Link>
          <Link className="rounded-xl border border-outline/30 px-6 py-3 text-on-surface-muted transition hover:border-primary hover:text-primary" href="/">
            오라클로 돌아가기
          </Link>
        </div>
      </section>
    </main>
  );
}
