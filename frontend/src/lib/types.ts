export type Role = "JOB_SEEKER" | "RECRUITER";
export type ApplicationStatus = "APPLIED" | "SHORTLISTED" | "INTERVIEW" | "OFFERED" | "REJECTED" | "WITHDRAWN";
export type SkillGap = { missingRequiredSkills: string[]; missingPreferredSkills: string[]; coveragePercentage: number; priorityOrderedMissingSkills: string[]; suggestions: Record<string, string> };
export type Recommendation = { jobId: string; title: string; company: string; location: string; matchPercent: number; matchedSkills: string[]; missingSkills: string[]; skillGap: SkillGap };
export type Job = { id: string; title: string; company: string; location: string; description: string; status: "OPEN" | "CLOSED"; workMode: string; salaryMin: number; salaryMax: number; minExperienceYears: number; skills: { name: string; importance: "REQUIRED" | "PREFERRED" }[]; certifications: string[] };
export type Application = { id: string; jobId: string; candidateId: string; status: ApplicationStatus; createdAt: string; updatedAt: string; job?: Pick<Job, "title" | "company"> };
export type Profile = { userId: string; headline: string; summary: string; location: string; workMode: string; education: string; experienceYears: number; skills: string[]; certifications: string[]; salaryMin: number; salaryMax: number };
export type SkillSuggestion = { id: string; name: string; source: "RESUME" | "CERTIFICATE"; confidence: number; status: "PENDING" | "ACCEPTED" | "REJECTED"; sourceDocument?: string | null; needsReview?: boolean };
export type CandidateRanking = { candidateId: string; matchPercent: number; semanticScore: number | null; semanticUsed: boolean; matchedSkills: string[]; missingSkills: string[]; applicationStatus: ApplicationStatus };
export type SkippedCandidate = { candidateId: string; error: string };
export type CandidateRankingPage = Page<CandidateRanking> & { skippedCandidates: SkippedCandidate[] };
export type Session = { userId: string; email: string; role: Role; token: string };
export type Page<T> = { content: T[]; number: number; size: number; totalElements: number; totalPages: number; first: boolean; last: boolean };

export type NotificationType = "RECOMMENDED_JOB" | "APPLICATION_STATUS_CHANGED";
export type Notification = {
  id: string;
  userId: string;
  type: NotificationType;
  title: string;
  message: string;
  refId: string | null;
  read: boolean;
  createdAt: string;
};
