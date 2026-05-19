"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

import { useAuthStore } from "@/stores/authStore";
import { validateEmail, validateNickname, validatePassword } from "@/utils/validators";

export default function SignupPage() {
  const router = useRouter();
  const signup = useAuthStore((state) => state.signup);
  const isLoading = useAuthStore((state) => state.isLoading);
  const [form, setForm] = useState({
    nickname: "",
    email: "",
    password: "",
    requiredAgreed: false
  });
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const nextError =
      validateNickname(form.nickname) ??
      validateEmail(form.email) ??
      validatePassword(form.password) ??
      (!form.requiredAgreed ? "이용약관과 개인정보 처리방침에 동의해야 합니다." : null);

    if (nextError) {
      setError(nextError);
      return;
    }

    setError(null);
    try {
      await signup({
        email: form.email.trim(),
        password: form.password,
        nickname: form.nickname.trim(),
        termsAgreed: form.requiredAgreed,
        privacyAgreed: form.requiredAgreed,
        marketingAgreed: false
      });
      router.push("/consultations/new");
    } catch {
      setError("회원가입에 실패했습니다. 입력값 또는 중복 이메일을 확인해주세요.");
    }
  }

  return (
    <main className="mystic-bg flex min-h-screen items-center justify-center px-5 py-10 text-on-surface">
      <form className="vellum-panel w-full max-w-md rounded-3xl p-7 md:p-9" onSubmit={handleSubmit}>
        <p className="font-script text-4xl text-primary">Aetheria</p>
        <h1 className="mt-3 font-display text-4xl font-semibold">아르카나의 일원이 되세요</h1>
        <p className="mt-3 text-on-surface-muted">상담 기록을 안전하게 보관하기 위한 계정을 만듭니다.</p>

        <div className="mt-8 space-y-4">
          <input
            className="mystic-input rounded-xl px-4 py-3"
            onChange={(event) => setForm((current) => ({ ...current, nickname: event.target.value }))}
            placeholder="당신이 불릴 이름"
            type="text"
            value={form.nickname}
          />
          <input
            className="mystic-input rounded-xl px-4 py-3"
            onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
            placeholder="seeker@example.com"
            type="email"
            value={form.email}
          />
          <input
            className="mystic-input rounded-xl px-4 py-3"
            onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
            placeholder="비밀번호를 입력하세요"
            type="password"
            value={form.password}
          />
        </div>

        <label className="mt-5 flex items-start gap-3 text-sm text-on-surface-muted">
          <input
            checked={form.requiredAgreed}
            className="mt-1 h-4 w-4 accent-primary"
            onChange={(event) => setForm((current) => ({ ...current, requiredAgreed: event.target.checked }))}
            type="checkbox"
          />
          <span>이용약관과 개인정보 처리방침에 동의합니다.</span>
        </label>

        {error && (
          <p className="mt-5 rounded-xl border border-red-300/25 bg-red-950/30 px-4 py-3 text-sm text-red-100">
            {error}
          </p>
        )}

        <button className="gold-button mt-7 w-full rounded-xl px-6 py-4 font-semibold" disabled={isLoading} type="submit">
          {isLoading ? "계정을 여는 중..." : "계정 만들기"}
        </button>

        <p className="mt-6 text-center text-sm text-on-surface-muted">
          이미 탐구자이신가요?{" "}
          <Link className="text-primary transition hover:underline" href="/login">
            오라클로 돌아가기
          </Link>
        </p>
      </form>
    </main>
  );
}
