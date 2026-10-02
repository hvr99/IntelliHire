import { applications as seedApplications, jobs as seedJobs, profile as seedProfile, recommendations as seedRecommendations, recruiterSession, seekerSession } from "./mock-data";
import type { Application, ApplicationStatus, CandidateRankingPage, Job, Notification, Page, Profile, Recommendation, Role, Session, SkillSuggestion } from "./types";

const useMocks = process.env.NEXT_PUBLIC_USE_MOCKS !== "false";
const apiBase = process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "");
const urls = {
  auth: apiBase ?? process.env.NEXT_PUBLIC_AUTH_URL ?? "http://localhost:8081",
  user: apiBase ?? process.env.NEXT_PUBLIC_USER_URL ?? "http://localhost:8082",
  job: apiBase ?? process.env.NEXT_PUBLIC_JOB_URL ?? "http://localhost:8083",
  application: apiBase ?? process.env.NEXT_PUBLIC_APPLICATION_URL ?? "http://localhost:8084",
  recommendation: apiBase ?? process.env.NEXT_PUBLIC_RECOMMENDATION_URL ?? "http://localhost:8085",
  notification: apiBase ?? process.env.NEXT_PUBLIC_NOTIFICATION_URL ?? "http://localhost:8086",
};
const wait = <T,>(value: T): Promise<T> => new Promise(resolve => setTimeout(() => resolve(value), 180));
let mockApplications = [...seedApplications];
let mockJobs = [...seedJobs];
let mockProfile: Profile = { ...seedProfile, skills: [...seedProfile.skills] };
let mockSuggestions: SkillSuggestion[] = [
  { id: "suggestion-python", name: "Python", source: "RESUME", confidence: 0.96, status: "PENDING", sourceDocument: "resume.pdf" },
  { id: "suggestion-docker", name: "Docker", source: "CERTIFICATE", confidence: 0.72, status: "PENDING", sourceDocument: "cloud-certificate.png", needsReview: true },
];
const mockCandidateRanking: CandidateRankingPage = { content: [{ candidateId: "candidate-1", matchPercent: 92, semanticScore: 0.88, semanticUsed: true, matchedSkills: ["typescript", "react"], missingSkills: ["accessibility"], applicationStatus: "SHORTLISTED" }, { candidateId: "candidate-2", matchPercent: 74, semanticScore: null, semanticUsed: false, matchedSkills: ["react"], missingSkills: ["node.js"], applicationStatus: "APPLIED" }], number: 0, size: 20, totalElements: 2, totalPages: 1, first: true, last: true, skippedCandidates: [] };

export class ApiError extends Error { constructor(public readonly status: number, message: string) { super(message); } }

function redirectOnUnauthorized(status: number) { if (status === 401 && typeof window !== "undefined") { window.sessionStorage.removeItem("jobrec_session"); window.location.assign("/login"); } }

async function request<T>(baseUrl: string, path: string, session?: Session, init?: RequestInit): Promise<T> {
  let response: Response;
  const headers = new Headers(init?.headers);
  headers.set("Accept", "application/json");
  if (!(init?.body instanceof FormData)) headers.set("Content-Type", "application/json");
  if (session?.token) headers.set("Authorization", `Bearer ${session.token}`);
  try { response = await fetch(`${baseUrl}${path}`, { ...init, headers }); }
  catch { throw new ApiError(0, "The service is unavailable. Check that the backend is running."); }
  if (!response.ok) { redirectOnUnauthorized(response.status); let message = `Request failed (${response.status})`; try { const body = await response.json() as { message?: string; error?: string }; message = body.message ?? body.error ?? message; } catch { /* non-JSON response */ } throw new ApiError(response.status, message); }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

function toSession(token: string, user: { id: string; email: string; role: Role }): Session { return { userId: user.id, email: user.email, role: user.role, token }; }
function toFrontendProfile(value: Partial<Profile>, userId: string, skills: string[] = []): Profile { return { userId, headline: value.headline ?? "", summary: value.summary ?? "", location: value.location ?? "", workMode: value.workMode ?? "", education: value.education ?? "", experienceYears: value.experienceYears ?? 0, skills: value.skills ?? skills, certifications: value.certifications ?? [], salaryMin: value.salaryMin ?? 0, salaryMax: value.salaryMax ?? 0 }; }

export const api = {
  async login(email: string, password: string, role: Role): Promise<Session> { if (useMocks) return wait(role === "RECRUITER" ? recruiterSession : seekerSession); const auth = await request<{ token: string }>(urls.auth, "/api/auth/login", undefined, { method: "POST", body: JSON.stringify({ email, password }) }); const user = await request<{ id: string; email: string; role: Role }>(urls.auth, "/api/auth/me", { userId: "", email, role, token: auth.token }); return toSession(auth.token, user); },
  async register(input: { email: string; password: string; role: Role }): Promise<Session> { if (useMocks) return wait(input.role === "RECRUITER" ? recruiterSession : seekerSession); await request<void>(urls.auth, "/api/auth/register", undefined, { method: "POST", body: JSON.stringify(input) }); return this.login(input.email, input.password, input.role); },
  async getProfile(session: Session): Promise<Profile> { if (useMocks) return wait(mockProfile); const [profile, skills] = await Promise.all([request<Partial<Profile>>(urls.user, "/api/users/me/profile", session), request<{ name: string }[]>(urls.user, "/api/users/me/skills", session)]); return toFrontendProfile(profile, session.userId, skills.map(skill => skill.name)); },
  async saveProfile(profile: Profile, session: Session): Promise<Profile> { if (useMocks) return wait(profile); const saved = await request<Partial<Profile>>(urls.user, "/api/users/me/profile", session, { method: "PUT", body: JSON.stringify({ userId: session.userId, headline: profile.headline, summary: profile.summary, location: profile.location, phone: "" }) }); return toFrontendProfile(saved, session.userId, profile.skills); },
  async uploadResume(file: File, session: Session): Promise<SkillSuggestion[]> { if (useMocks) return wait(mockSuggestions.filter(suggestion => suggestion.status === "PENDING")); const form = new FormData(); form.append("file", file); return request<SkillSuggestion[]>(urls.user, "/api/profile/resume", session, { method: "POST", body: form }); },
  async uploadCertificate(file: File, session: Session): Promise<SkillSuggestion[]> { if (useMocks) return wait(mockSuggestions.filter(suggestion => suggestion.status === "PENDING")); const form = new FormData(); form.append("file", file); return request<SkillSuggestion[]>(urls.user, "/api/profile/certificates", session, { method: "POST", body: form }); },
  async getSkillSuggestions(session: Session): Promise<SkillSuggestion[]> { if (useMocks) return wait(mockSuggestions); return request<SkillSuggestion[]>(urls.user, "/api/profile/skill-suggestions", session); },
  async confirmSkillSuggestions(suggestionIds: string[], session: Session): Promise<SkillSuggestion[]> { if (useMocks) { const selected = new Set(suggestionIds); mockSuggestions = mockSuggestions.map(suggestion => suggestion.status === "PENDING" ? { ...suggestion, status: selected.has(suggestion.id) ? "ACCEPTED" : "REJECTED" } : suggestion); mockProfile = { ...mockProfile, skills: Array.from(new Set([...mockProfile.skills, ...mockSuggestions.filter(suggestion => suggestion.status === "ACCEPTED").map(suggestion => suggestion.name)])) }; return wait(mockSuggestions); } return request<SkillSuggestion[]>(urls.user, "/api/profile/skill-suggestions/confirm", session, { method: "POST", body: JSON.stringify({ suggestionIds }) }); },
  async getJobs(query = "", session?: Session): Promise<Job[]> { if (useMocks) return wait(mockJobs.filter(job => `${job.title} ${job.company} ${job.location}`.toLowerCase().includes(query.toLowerCase()))); const params = new URLSearchParams({ page: "0", size: "100" }); if (query) params.set("keyword", query); const path = session?.role === "RECRUITER" ? `/api/jobs/mine?${params}` : `/api/jobs?${params}`; const page = await request<Page<Job>>(urls.job, path, session); return page.content; },
  async getJob(id: string, session?: Session): Promise<Job> { if (useMocks) { const job = mockJobs.find(item => item.id === id); if (!job) throw new ApiError(404, "Job not found"); return wait(job); } return request<Job>(urls.job, `/api/jobs/${id}`, session); },
  async getRecommendations(session: Session, page = 0, size = 20, minScore?: number): Promise<Page<Recommendation>> { if (useMocks) { const filtered = seedRecommendations.filter(item => minScore === undefined || item.matchPercent >= minScore); return wait({ content: filtered.slice(page * size, page * size + size), number: page, size, totalElements: filtered.length, totalPages: Math.ceil(filtered.length / size), first: page === 0, last: page >= Math.ceil(filtered.length / size) - 1 }); } const params = new URLSearchParams({ page: String(page), size: String(size) }); if (minScore !== undefined) params.set("minScore", String(minScore)); return request<Page<Recommendation>>(urls.recommendation, `/api/recommendations?${params}`, session); },
  async getRecommendation(id: string, session: Session): Promise<Recommendation> { if (useMocks) { const item = seedRecommendations.find(rec => rec.jobId === id); if (!item) throw new ApiError(404, "Recommendation not found"); return wait(item); } return request<Recommendation>(urls.recommendation, `/api/recommendations/jobs/${id}`, session); },
  async apply(jobId: string, session: Session): Promise<Application> { if (useMocks) { const application: Application = { id: `application-${Date.now()}`, jobId, candidateId: session.userId, status: "APPLIED", createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), job: { title: mockJobs.find(job => job.id === jobId)?.title ?? "Job", company: mockJobs.find(job => job.id === jobId)?.company ?? "Company" } }; mockApplications = [application, ...mockApplications]; return wait(application); } return request<Application>(urls.application, "/api/applications", session, { method: "POST", body: JSON.stringify({ jobId }) }); },
  async getApplications(session: Session, jobId?: string): Promise<Application[]> { if (useMocks) return wait(mockApplications); const path = session.role === "JOB_SEEKER" ? "/api/applications/me" : `/api/applications/job/${jobId ?? ""}`; return request<Application[]>(urls.application, path, session); },
  async getCandidateRanking(jobId: string, session: Session, page = 0, size = 20): Promise<CandidateRankingPage> { if (useMocks) { const content = mockCandidateRanking.content.slice(page * size, page * size + size); const totalPages = Math.ceil(mockCandidateRanking.totalElements / size); return wait({ ...mockCandidateRanking, content, number: page, size, totalPages, first: page === 0, last: page >= totalPages - 1 }); } return request<CandidateRankingPage>(urls.recommendation, `/api/recruiter/jobs/${jobId}/candidates?page=${page}&size=${size}`, session); },
  async getCandidateProfile(candidateId: string, session: Session): Promise<Profile> { if (useMocks) return wait({ ...mockProfile, userId: candidateId }); const profile = await request<Partial<Profile>>(urls.user, `/api/candidates/${candidateId}/profile`, session); return toFrontendProfile(profile, candidateId); },
  async updateApplicationStatus(id: string, status: ApplicationStatus, session: Session): Promise<Application> { if (useMocks) { const current = mockApplications.find(item => item.id === id); if (!current) throw new ApiError(404, "Application not found"); current.status = status; current.updatedAt = new Date().toISOString(); return wait(current); } return request<Application>(urls.application, `/api/applications/${id}/status`, session, { method: "PATCH", body: JSON.stringify({ status }) }); },
  async createJob(input: Partial<Job>, session: Session): Promise<Job> { if (useMocks) { const job = { ...mockJobs[0], ...input, id: `job-${Date.now()}` } as Job; mockJobs = [job, ...mockJobs]; return wait(job); } return request<Job>(urls.job, "/api/jobs", session, { method: "POST", body: JSON.stringify(input) }); },
  async updateJob(id: string, input: Partial<Job>, session: Session): Promise<Job> { if (useMocks) { const index = mockJobs.findIndex(job => job.id === id); mockJobs[index] = { ...mockJobs[index], ...input }; return wait(mockJobs[index]); } return request<Job>(urls.job, `/api/jobs/${id}`, session, { method: "PUT", body: JSON.stringify(input) }); },
  async getNotifications(session: Session): Promise<Notification[]> { if (useMocks) return wait([]); return request<Notification[]>(urls.notification, "/api/notifications", session); },
  async markNotificationRead(id: string, session: Session): Promise<void> { if (useMocks) return wait(undefined); return request<void>(urls.notification, `/api/notifications/${id}/read`, session, { method: "POST" }); },
  async markAllNotificationsRead(session: Session): Promise<void> { if (useMocks) return wait(undefined); return request<void>(urls.notification, "/api/notifications/read-all", session, { method: "POST" }); },
  serviceUrls: urls,
};
