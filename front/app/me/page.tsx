"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

import { useAuthStore } from "@/stores/authStore";

export default function MyPage() {
  const router = useRouter();
  const profile = useAuthStore((state) => state.profile);
  const isLoading = useAuthStore((state) => state.isLoading);
  const loadProfile = useAuthStore((state) => state.loadProfile);
  const logout = useAuthStore((state) => state.logout);
  const requestWithdrawal = useAuthStore((state) => state.requestWithdrawal);
  const [error, setError] = useState<string | null>(null);
  const [isWithdrawing, setIsWithdrawing] = useState(false);

  useEffect(() => {
    loadProfile().catch(() => setError("내 정보를 불러오지 못했습니다. 로그인 상태를 확인해주세요."));
  }, [loadProfile]);

  async function handleLogout() {
    await logout();
    router.push("/");
  }

  async function handleWithdrawal() {
    setIsWithdrawing(true);
    setError(null);
    try {
      await requestWithdrawal();
      router.push("/");
    } catch {
      setError("회원 탈퇴 요청에 실패했습니다.");
      setIsWithdrawing(false);
    }
  }

  return (
    <main className="min-h-screen bg-background bg-mystic-radial px-5 py-8 text-on-surface">
      <section className="mx-auto max-w-3xl">
        <header className="mb-8 flex items-center justify-between">
          <div>
            <p className="font-script text-3xl text-primary">My Page</p>
            <h1 className="mt-2 font-display text-4xl font-semibold">마이페이지</h1>
          </div>
          <Link className="text-sm text-on-surface-muted transition hover:text-primary" href="/">
            홈으로
          </Link>
        </header>

        {error && (
          <p className="mb-6 rounded-xl border border-red-300/25 bg-red-950/30 px-4 py-3 text-sm text-red-100">
            {error}
          </p>
        )}

        <section className="vellum-panel rounded-3xl p-6 md:p-8">
          {isLoading && <p className="text-on-surface-muted">내 정보를 불러오고 있습니다...</p>}

          {profile && (
            <div className="space-y-4">
              <ProfileRow label="이메일" value={profile.email} />
              <ProfileRow label="닉네임" value={profile.nickname} />
              <ProfileRow label="계정 상태" value={statusLabel(profile.status)} />
              <ProfileRow label="가입일" value={formatDate(profile.createdAt)} />
            </div>
          )}

          <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-end">
            <button
              className="rounded-xl border border-outline/30 px-6 py-3 text-on-surface-muted transition hover:border-primary hover:text-primary"
              onClick={handleLogout}
              type="button"
            >
              로그아웃
            </button>
            <button
              className="rounded-xl border border-red-300/30 px-6 py-3 text-red-100 transition hover:border-red-200"
              disabled={isWithdrawing}
              onClick={handleWithdrawal}
              type="button"
            >
              {isWithdrawing ? "요청 중..." : "회원 탈퇴 요청"}
            </button>
          </div>
        </section>
      </section>
    </main>
  );
}

function ProfileRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="border-b border-outline/20 pb-4 last:border-b-0 last:pb-0">
      <p className="text-sm text-on-surface-muted">{label}</p>
      <p className="mt-1 text-xl">{value}</p>
    </div>
  );
}

function statusLabel(status: string) {
  if (status === "ACTIVE") {
    return "활성";
  }
  if (status === "WITHDRAWAL_REQUESTED") {
    return "탈퇴 요청됨";
  }
  return "탈퇴 완료";
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}
