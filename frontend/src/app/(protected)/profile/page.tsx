"use client";

import { useCallback, useEffect, useState } from "react";
import { ApiError, api } from "@/lib/api";
import type { Profile as ProfileData, SkillSuggestion } from "@/lib/types";
import { useSession } from "@/components/SessionProvider";
import { RoleGuard } from "@/components/RoleGuard";
import { PageHeading, Field } from "@/components/FormBits";
import { LoadingState } from "@/components/AsyncState";
import { DocumentUpload } from "@/components/DocumentUpload";
import { SkillSuggestions } from "@/components/SkillSuggestions";

export default function ProfilePage() { return <RoleGuard role="JOB_SEEKER"><Profile /></RoleGuard>; }

function Profile() {
  const { session } = useSession();
  const [data, setData] = useState<ProfileData | null>(null);
  const [suggestions, setSuggestions] = useState<SkillSuggestion[]>([]);
  const [loadingSuggestions, setLoadingSuggestions] = useState(true);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState("");
  const [uploadError, setUploadError] = useState("");
  const [confirming, setConfirming] = useState(false);

  const load = useCallback(async () => {
    if (!session) return;
    setError("");
    try { const [profile, pending] = await Promise.all([api.getProfile(session), api.getSkillSuggestions(session)]); setData(profile); setSuggestions(pending); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "Unable to load your profile."); }
    finally { setLoadingSuggestions(false); }
  }, [session]);
  useEffect(() => { void load(); }, [load]);
  if (error && !data) return <div className="rounded-2xl border border-red-200 bg-red-50 p-5 text-sm text-red-700"><p className="font-bold">We couldn’t load your profile.</p><p className="mt-1">{error}</p><button className="mt-3 font-bold underline" onClick={() => void load()}>Try again</button></div>;
  if (!data) return <LoadingState />;

  function change(key: keyof ProfileData, value: string | number) { setData(previous => previous ? { ...previous, [key]: ["experienceYears", "salaryMin", "salaryMax"].includes(key) ? Number(value) : value } : previous); }
  async function save() { if (!session || !data) return; try { setData(await api.saveProfile(data, session)); setSaved(true); setTimeout(() => setSaved(false), 2200); } catch (cause) { setError(cause instanceof Error ? cause.message : "Unable to save your profile."); } }
  async function upload(kind: "resume" | "certificate", file: File) { if (!session) return; setUploadError(""); try { const result = kind === "resume" ? await api.uploadResume(file, session) : await api.uploadCertificate(file, session); setSuggestions(result); } catch (cause) { setUploadError(cause instanceof ApiError && cause.status === 503 ? "Skill extraction is temporarily unavailable. Your profile was not changed; please try again later." : cause instanceof Error ? cause.message : "The document could not be processed."); } }
  async function confirm(ids: string[]) { if (!session) return; setConfirming(true); try { await api.confirmSkillSuggestions(ids, session); await load(); } catch (cause) { setUploadError(cause instanceof Error ? cause.message : "Unable to confirm these skills."); } finally { setConfirming(false); } }

  return <>
    <PageHeading eyebrow="Your foundation" title="My profile" text="A complete profile gives the matching engine more signal." action={saved ? <span className="text-sm font-bold text-emerald-600">Saved ✓</span> : <button className="btn-primary" onClick={save}>Save changes</button>} />
    <div className="grid gap-5 lg:grid-cols-2">
      <section className="card space-y-5 p-6"><h2 className="text-lg font-black">About you</h2><Field label="Headline" value={data.headline} onChange={event => change("headline", event.target.value)} /><label className="block text-sm font-bold">Summary<textarea className="field min-h-28" value={data.summary} onChange={event => change("summary", event.target.value)} /></label><Field label="Location" value={data.location} onChange={event => change("location", event.target.value)} /></section>
      <section className="card space-y-5 p-6"><h2 className="text-lg font-black">Experience & preferences</h2><Field label="Years of experience" type="number" value={data.experienceYears} onChange={event => change("experienceYears", event.target.value)} /><Field label="Education" value={data.education} onChange={event => change("education", event.target.value)} /><Field label="Preferred work mode" value={data.workMode} onChange={event => change("workMode", event.target.value)} /><div className="grid gap-4 sm:grid-cols-2"><Field label="Salary min" type="number" value={data.salaryMin} onChange={event => change("salaryMin", event.target.value)} /><Field label="Salary max" type="number" value={data.salaryMax} onChange={event => change("salaryMax", event.target.value)} /></div></section>
      <section className="card p-6"><h2 className="text-lg font-black">Confirmed skills</h2>{data.skills.length ? <div className="mt-4 flex flex-wrap gap-2">{data.skills.map(skill => <span className="tag bg-mint text-emerald-700" key={skill}>{skill}</span>)}</div> : <p className="mt-3 text-sm text-slate-500">No confirmed skills yet.</p>}<p className="mt-4 text-xs text-slate-500">Extracted skills appear here only after you confirm them.</p></section>
      <section className="card p-6"><h2 className="text-lg font-black">Certifications</h2>{data.certifications.length ? <div className="mt-4 flex flex-wrap gap-2">{data.certifications.map(cert => <span className="tag" key={cert}>{cert}</span>)}</div> : <p className="mt-3 text-sm text-slate-500">No certifications added yet.</p>}<p className="mt-4 text-xs text-slate-500">Certificates can suggest skills without changing your profile automatically.</p></section>
    </div>
    <section className="mt-5 card space-y-4 p-6"><div><h2 className="text-lg font-black">Discover skills from documents</h2><p className="mt-1 text-sm text-slate-500">Resume upload is optional. Review every suggestion before confirming it.</p></div><DocumentUpload kind="resume" onUpload={file => upload("resume", file)} /><DocumentUpload kind="certificate" onUpload={file => upload("certificate", file)} />{uploadError && <p role="alert" className="rounded-xl bg-red-50 p-3 text-sm font-semibold text-red-700">{uploadError}</p>}</section>
    <div className="mt-5"><SkillSuggestions suggestions={suggestions} loading={loadingSuggestions} confirming={confirming} onConfirm={confirm} /></div>
  </>;
}
