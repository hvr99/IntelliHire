"use client";

import React from "react";
import Link from "next/link";
import type { ApplicationStatus, CandidateRanking } from "@/lib/types";

const statuses: ApplicationStatus[] = ["APPLIED", "SHORTLISTED", "INTERVIEW", "OFFERED", "REJECTED", "WITHDRAWN"];

type Props = { candidates: CandidateRanking[]; applicationIds: Record<string, string | undefined>; updatingId?: string; onStatusChange: (applicationId: string, status: ApplicationStatus) => void };

export function CandidateRankingList({ candidates, applicationIds, updatingId, onStatusChange }: Props) {
  return <div className="space-y-3">{candidates.map((candidate, index) => {
    const applicationId = applicationIds[candidate.candidateId];
    const hasSemanticScore = candidate.semanticScore !== null && candidate.semanticScore !== undefined;
    return <article className="card p-5" key={candidate.candidateId}>
      <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
        <div className="min-w-0"><p className="eyebrow">Rank #{index + 1}</p><Link className="mt-1 block text-lg font-black text-ink hover:text-coral" href={`/candidates/${candidate.candidateId}/profile`}>Candidate {candidate.candidateId.slice(0, 8)} →</Link><div className="mt-3 flex flex-wrap items-center gap-2"><span className="rounded-full bg-mint px-3 py-1 text-sm font-black text-emerald-700">{Math.round(candidate.matchPercent)}% match</span>{hasSemanticScore && <span className="tag">Semantic {Math.round(candidate.semanticScore! * 100)}%</span>}<span className="tag">{candidate.applicationStatus}</span></div></div>
        <label className="text-sm font-bold text-slate-600">Application status<select className="field min-w-48" disabled={!applicationId || updatingId === applicationId} value={candidate.applicationStatus} onChange={event => { if (applicationId) onStatusChange(applicationId, event.target.value as ApplicationStatus); }}>{statuses.map(status => <option value={status} key={status}>{status}</option>)}</select></label>
      </div>
      <div className="mt-5 grid gap-4 text-sm md:grid-cols-2"><SkillList title="Matched skills" skills={candidate.matchedSkills} tone="green" /><SkillList title="Missing skills" skills={candidate.missingSkills} tone="amber" /></div>
    </article>;
  })}</div>;
}

function SkillList({ title, skills, tone }: { title: string; skills: string[]; tone: "green" | "amber" }) {
  return <div><p className="text-xs font-bold uppercase tracking-wider text-slate-400">{title}</p>{skills.length ? <div className="mt-2 flex flex-wrap gap-2">{skills.map(skill => <span className={`tag ${tone === "green" ? "bg-mint text-emerald-700" : "bg-amber-50 text-amber-700"}`} key={skill}>{skill}</span>)}</div> : <p className="mt-2 text-slate-500">None</p>}</div>;
}
