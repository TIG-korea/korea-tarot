"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

import { useAuthStore } from "@/stores/authStore";
import { validateEmail, validatePassword } from "@/utils/validators";

export default function LoginPage() {
  const router = useRouter();
  const login = useAuthStore((state) => state.login);
  const isLoading = useAuthStore((state) => state.isLoading);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const nextError = validateEmail(email) ?? validatePassword(password);
    if (nextError) {
      setError(nextError);
      return;
    }

    setError(null);
    try {
      await login({ email: email.trim(), password });
      router.push("/consultations/new");
    } catch {
      setError("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
  }

  return (
    <main className="mystic-bg flex min-h-screen items-center justify-center px-5 py-10 text-on-surface">
      <form className="vellum-panel w-full max-w-md rounded-3xl p-7 md:p-9" onSubmit={handleSubmit}>
        <p className="font-script text-5xl text-primary">에테리아</p>
        <h1 className="mt-3 font-display text-4xl font-semibold">다시 오신 것을 환영합니다</h1>
        <p className="mt-3 text-on-surface-muted">계정으로 들어와 상담을 이어가세요.</p>

        <div className="mt-8 space-y-4">
          <input
            className="mystic-input rounded-xl px-4 py-3"
            onChange={(event) => setEmail(event.target.value)}
            placeholder="seeker@cosmos.net"
            type="email"
            value={email}
          />
          <input
            className="mystic-input rounded-xl px-4 py-3"
            onChange={(event) => setPassword(event.target.value)}
            placeholder="비밀번호"
            type="password"
            value={password}
          />
        </div>

        {error && (
          <p className="mt-5 rounded-xl border border-red-300/25 bg-red-950/30 px-4 py-3 text-sm text-red-100">
            {error}
          </p>
        )}

        <button className="gold-button mt-7 w-full rounded-xl px-6 py-4 font-semibold" disabled={isLoading} type="submit">
          {isLoading ? "문을 여는 중..." : "장막 열기"}
        </button>

        <p className="mt-6 text-center text-sm text-on-surface-muted">
          성소가 처음이신가요?{" "}
          <Link className="text-primary transition hover:underline" href="/signup">
            새로운 계정 만들기
          </Link>
        </p>
      </form>
    </main>
  );
}
