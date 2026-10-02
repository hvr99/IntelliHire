"use client";

import { useCallback, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { api, ApiError } from "@/lib/api";
import type { Application, ApplicationStatus, CandidateRankingPage } from "@/lib/types";
import { useSession } from "@/components/SessionProvider";
import { PageHeading } from "@/components/FormBits";
import { CandidateRankingList } from "@/components/CandidateRankingList";
import { EmptyState, LoadingState } from "@/components/AsyncState";

export default function CandidateRankingPage() {
  const { session } = useSession();
  const params = useParams<{ jobId: string }>();
  const [data, setData] = useState<CandidateRankingPage | null>(null);
  const [applications, setApplications] = useState<Application[]>([]);
  const [page, setPage] = useState(0);
  const [error, setError] = useState("");
  const [updatingId, setUpdatingId] = useState("");

  const load = useCallback(async () => {
    if (!session) return;
    setError("");
    setData(null);
    try {
      const [ranking, jobApplications] = await Promise.all([api.getCandidateRanking(params.jobId, session, page), api.getApplications(session, params.jobId)]);
      setData(ranking); setApplications(jobApplications);
    } catch (cause) { setError(cause instanceof ApiError ? cause.message : "Unable to load candidate rankings."); }
  }, [page, params.jobId, session]);
  useEffect(() => { void load(); }, [load]);

  async function changeStatus(applicationId: string, status: ApplicationStatus) {
    if (!session) return;
    setUpdatingId(applicationId);
    try { const updated = await api.updateApplicationStatus(applicationId, status, session); setApplications(current => current.map(application => application.id === updated.id ? updated : application)); setData(current => current ? { ...current, content: current.content.map(candidate => applications.find(application => application.id === applicationId)?.candidateId === candidate.candidateId ? { ...candidate, applicationStatus: updated.status } : candidate) } : current); }
    catch (cause) { setError(cause instanceof ApiError ? cause.message : "Unable to update application status."); }
    finally { setUpdatingId(""); }
  }

  const applicationIds = Object.fromEntries(applications.map(application => [application.candidateId, application.id]));
  return <><PageHeading eyebrow="Recruiter workspace" title="Ranked candidates" text="Candidates are ranked with the same weighted matching engine used by recommendations." />{error ? <div className="rounded-2xl border border-red-200 bg-red-50 p-5 text-sm text-red-700"><p className="font-bold">We couldn’t load this shortlist.</p><p className="mt-1">{error}</p><button className="mt-3 font-bold underline" onClick={() => void load()}>Try again</button></div> : !data ? <LoadingState label="Ranking candidates" /> : data.content.length === 0 ? <EmptyState title="No ranked candidates" text="Applicants with available profiles will appear here." /> : <><CandidateRankingList candidates={data.content} applicationIds={applicationIds} updatingId={updatingId} onStatusChange={(id, status) => void changeStatus(id, status)} />{data.skippedCandidates.length > 0 && <div className="mt-5 rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800"><p className="font-bold">Some candidates were skipped</p>{data.skippedCandidates.map(skipped => <p className="mt-1" key={skipped.candidateId}>{skipped.candidateId}: {skipped.error}</p>)}</div>}<div className="mt-5 flex items-center justify-between"><button className="btn-secondary" disabled={data.first} onClick={() => setPage(current => current - 1)}>Previous</button><span className="text-sm text-slate-500">Page {data.number + 1} of {Math.max(data.totalPages, 1)}</span><button className="btn-secondary" disabled={data.last} onClick={() => setPage(current => current + 1)}>Next</button></div></>}</>;
}
