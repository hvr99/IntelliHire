"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { api } from "@/lib/api";
import type { Profile } from "@/lib/types";
import { useSession } from "@/components/SessionProvider";
import { PageHeading } from "@/components/FormBits";
import { LoadingState } from "@/components/AsyncState";

export default function CandidateProfilePage() {
  const { session } = useSession();
  const { candidateId } = useParams<{ candidateId: string }>();
  const [profile, setProfile] = useState<Profile | null>(null);
  useEffect(() => { if (session) api.getCandidateProfile(candidateId, session).then(setProfile).catch(() => setProfile(null)); }, [candidateId, session]);
  if (!profile) return <LoadingState label="Loading candidate profile" />;
  return <><PageHeading eyebrow="Candidate profile" title={profile.headline || "Candidate profile"} text={profile.summary || "No summary provided."} /><div className="grid gap-5 lg:grid-cols-2"><section className="card p-6"><p className="eyebrow">Skills</p><div className="mt-4 flex flex-wrap gap-2">{profile.skills.length ? profile.skills.map(skill => <span className="tag bg-mint text-emerald-700" key={skill}>{skill}</span>) : <span className="text-sm text-slate-500">No skills listed.</span>}</div></section><section className="card p-6"><p className="eyebrow">Experience & location</p><p className="mt-3 text-sm">{profile.experienceYears} years · {profile.location || "Location not provided"}</p><p className="mt-2 text-sm">{profile.education || "Education not provided"}</p></section></div></>;
}
