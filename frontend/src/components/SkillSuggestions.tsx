"use client";

import { useEffect, useState } from "react";
import { EmptyState } from "./AsyncState";
import type { SkillSuggestion } from "@/lib/types";

type Props = { suggestions: SkillSuggestion[]; loading?: boolean; confirming?: boolean; onConfirm: (ids: string[]) => Promise<void> | void };

export function toggleSuggestionSelection(selected: Set<string>, id: string): Set<string> {
  const next = new Set(selected);
  if (next.has(id)) next.delete(id); else next.add(id);
  return next;
}

export function SkillSuggestions({ suggestions, loading = false, confirming = false, onConfirm }: Props) {
  const pending = suggestions.filter(suggestion => suggestion.status === "PENDING");
  const [selected, setSelected] = useState<Set<string>>(new Set());
  useEffect(() => setSelected(new Set(suggestions.filter(suggestion => suggestion.status === "PENDING").map(suggestion => suggestion.id))), [suggestions]);
  if (loading) return <div className="card p-6 text-sm text-slate-500">Loading skill suggestions…</div>;
  if (!pending.length) return <EmptyState title="No pending suggestions" text="Upload a resume or certificate to discover skills for your review." />;
  function toggle(id: string) { setSelected(previous => toggleSuggestionSelection(previous, id)); }
  return <section className="card p-6" aria-label="Skill suggestions"><div className="flex items-start justify-between gap-4"><div><h2 className="text-lg font-black">Review extracted skills</h2><p className="mt-1 text-sm text-slate-500">Nothing is added to your profile until you confirm it.</p></div><button className="btn-primary" disabled={confirming} onClick={() => void onConfirm(Array.from(selected))}>{confirming ? "Confirming…" : "Confirm selected"}</button></div><div className="mt-5 space-y-3">{pending.map(suggestion => <label className="flex cursor-pointer items-center gap-3 rounded-xl border border-slate-200 p-3" key={suggestion.id}><input type="checkbox" checked={selected.has(suggestion.id)} onChange={() => toggle(suggestion.id)} /><span className="min-w-0 flex-1"><span className="block font-bold">{suggestion.name}</span><span className="text-xs text-slate-500">{suggestion.source} · {Math.round(suggestion.confidence * 100)}% confidence{suggestion.sourceDocument ? ` · ${suggestion.sourceDocument}` : ""}</span>{(suggestion.needsReview || suggestion.confidence < 0.8) && <span className="ml-2 text-xs font-bold text-amber-700">Review carefully</span>}</span></label>)}</div></section>;
}
