"use client";
import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { api } from "@/lib/api";
import type { Application, ApplicationStatus } from "@/lib/types";
import { useSession } from "@/components/SessionProvider";
import { PageHeading } from "@/components/FormBits";
import { LoadingState, EmptyState, ErrorState } from "@/components/AsyncState";

const statuses: ApplicationStatus[] = ["APPLIED", "SHORTLISTED", "INTERVIEW", "OFFERED", "REJECTED"];
export default function RecruiterApplications() {
  const { session } = useSession(); const params = useSearchParams(); const jobId = params.get("jobId") ?? undefined;
  const [data, setData] = useState<Application[] | null>(null); const [error, setError] = useState(false);
  useEffect(() => { if (session) { setData(null); api.getApplications(session, jobId).then(setData).catch(() => setError(true)); } }, [session, jobId]);
  async function change(id: string, status: ApplicationStatus) { if (session) { const updated = await api.updateApplicationStatus(id, status, session); setData(items => items?.map(item => item.id === id ? updated : item) ?? []); } }
  return <><PageHeading eyebrow="Candidate pipeline" title="Applications" text="Review candidates and move each application to its next clear step." />{error ? <ErrorState retry={() => setError(false)} /> : !data ? <LoadingState /> : data.length === 0 ? <EmptyState title="No applications yet" text="Applications for your roles will appear here." /> : <div className="space-y-3">{data.map(application => <div className="card flex flex-col gap-5 p-5 lg:flex-row lg:items-center lg:justify-between" key={application.id}><div><p className="eyebrow">Application {application.id.slice(0, 8)}</p><h2 className="mt-1 text-lg font-black">Candidate application</h2><p className="mt-2 text-sm text-slate-500">{new Date(application.createdAt).toLocaleDateString()}</p></div><div className="flex flex-wrap items-center gap-2"><span className="mr-2 text-xs font-bold text-slate-400">Move to</span>{statuses.map(status => <button key={status} onClick={() => change(application.id, status)} className={`rounded-lg px-2.5 py-2 text-xs font-bold ${application.status === status ? "bg-ink text-white" : "bg-slate-100 text-slate-600 hover:bg-slate-200"}`}>{status}</button>)}</div></div>)}</div>}</>;
}
