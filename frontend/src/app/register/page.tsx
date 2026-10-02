"use client";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { api } from "@/lib/api";
import type { Role } from "@/lib/types";
import { useSession } from "@/components/SessionProvider";

export default function RegisterPage() {
  const router = useRouter();
  const { signIn } = useSession();
  const [role, setRole] = useState<Role>("JOB_SEEKER");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  async function submit(event: React.FormEvent) {
    event.preventDefault(); setLoading(true); setError("");
    try { const session = await api.register({ email, password, role }); signIn(session); router.push(role === "RECRUITER" ? "/recruiter" : "/dashboard"); }
    catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to create account"); }
    finally { setLoading(false); }
  }
  return <main className="flex min-h-screen items-center justify-center p-6"><div className="w-full max-w-md"><Link href="/login" className="text-xl font-black">orbit<span className="text-coral">.</span></Link><div className="card mt-10 p-7"><p className="eyebrow">Start your journey</p><h1 className="mt-3 text-3xl font-black">Create your workspace</h1><form onSubmit={submit} className="mt-7 space-y-5"><label className="block text-sm font-bold">Email<input className="field" type="email" required value={email} onChange={event => setEmail(event.target.value)} placeholder="you@example.com" /></label><label className="block text-sm font-bold">Password<input className="field" type="password" minLength={6} required value={password} onChange={event => setPassword(event.target.value)} /></label><div><p className="text-sm font-bold">I am joining as</p><div className="mt-2 grid grid-cols-2 gap-2">{(["JOB_SEEKER", "RECRUITER"] as Role[]).map(item => <button type="button" key={item} onClick={() => setRole(item)} className={`rounded-xl border px-3 py-3 text-sm font-bold ${role === item ? "border-ink bg-ink text-white" : "border-slate-200 text-slate-500"}`}>{item === "JOB_SEEKER" ? "Job seeker" : "Recruiter"}</button>)}</div></div>{error && <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</p>}<button disabled={loading} className="btn-primary w-full">{loading ? "Creating..." : "Create account"}</button></form></div><p className="mt-6 text-center text-sm text-slate-500">Already have an account? <Link href="/login" className="font-bold text-coral">Sign in</Link></p></div></main>;
}
