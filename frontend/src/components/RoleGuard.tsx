"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import type { Role, Session } from "@/lib/types";
import { useSession } from "./SessionProvider";

export function getRoleRedirect(session: Session | null, ready: boolean, role?: Role) {
  if (!ready) return null;
  if (!session) return "/login";
  if (role && session.role !== role) {
    return session.role === "RECRUITER" ? "/recruiter" : "/dashboard";
  }
  return null;
}

export function RoleGuard({ role, children }: { role?: Role; children: React.ReactNode }) {
  const { session, ready } = useSession();
  const router = useRouter();

  useEffect(() => {
    const redirect = getRoleRedirect(session, ready, role);
    if (redirect) router.replace(redirect);
  }, [ready, session, role, router]);

  if (!ready || !session || (role && session.role !== role)) {
    return <div className="flex min-h-screen items-center justify-center text-sm text-slate-500">Checking your workspace...</div>;
  }

  return <>{children}</>;
}
