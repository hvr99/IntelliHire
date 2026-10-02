"use client";

import { useRef, useState } from "react";

const resumeTypes = ["application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"];
const certificateTypes = ["application/pdf", "image/png", "image/jpeg"];

type Props = { kind: "resume" | "certificate"; onUpload: (file: File) => Promise<void>; disabled?: boolean; maxSizeBytes?: number };

export function validateDocumentFile(file: File | undefined, kind: Props["kind"], maxSizeBytes: number): string | undefined {
  if (!file) return `Choose a ${kind} file first.`;
  const allowed = kind === "resume" ? resumeTypes : certificateTypes;
  const extension = file.name.toLowerCase().split(".").pop();
  const allowedExtension = kind === "resume" ? ["pdf", "docx"] : ["pdf", "png", "jpg", "jpeg"];
  if (!allowed.includes(file.type) && !allowedExtension.includes(extension ?? "")) return `Unsupported ${kind} type.`;
  if (file.size === 0) return "The selected file is empty.";
  if (file.size > maxSizeBytes) return `The file must be smaller than ${Math.round(maxSizeBytes / (1024 * 1024))} MB.`;
  return undefined;
}

export function DocumentUpload({ kind, onUpload, disabled = false, maxSizeBytes = kind === "resume" ? 5 * 1024 * 1024 : 10 * 1024 * 1024 }: Props) {
  const input = useRef<HTMLInputElement>(null);
  const [error, setError] = useState("");
  const [uploading, setUploading] = useState(false);
  const accept = kind === "resume" ? ".pdf,.docx" : ".pdf,.png,.jpg,.jpeg";
  const label = kind === "resume" ? "Resume" : "Certificate";

  async function select(file?: File) {
    setError("");
    if (!file) { setError(`Choose a ${label.toLowerCase()} file first.`); return; }
    const validationError = validateDocumentFile(file, kind, maxSizeBytes);
    if (validationError) { setError(validationError); return; }
    setUploading(true);
    try { await onUpload(file); } finally { setUploading(false); }
  }

  return <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-4">
    <input ref={input} className="sr-only" type="file" accept={accept} disabled={disabled || uploading} onChange={event => { void select(event.target.files?.[0]); event.currentTarget.value = ""; }} />
    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div><p className="font-bold">Upload {label.toLowerCase()}</p><p className="mt-1 text-xs text-slate-500">{accept.replaceAll(",", ", ")} · max {Math.round(maxSizeBytes / (1024 * 1024))} MB</p></div>
      <button type="button" className="btn-secondary" disabled={disabled || uploading} onClick={() => input.current?.click()}>{uploading ? "Extracting…" : `Choose ${label.toLowerCase()}`}</button>
    </div>
    {error && <p role="alert" className="mt-3 text-sm font-semibold text-red-600">{error}</p>}
  </div>;
}
