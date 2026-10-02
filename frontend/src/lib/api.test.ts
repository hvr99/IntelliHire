import { describe, expect, it } from "vitest";
import { api } from "./api";
import { seekerSession } from "./mock-data";

describe("mock API client", () => {
  it("exposes independent service URLs for direct services or a gateway", () => {
    expect(api.serviceUrls.auth).toBe("http://localhost:8081");
    expect(api.serviceUrls.user).toBe("http://localhost:8082");
    expect(api.serviceUrls.recommendation).toBe("http://localhost:8085");
  });

  it("returns deterministic recommendations sorted by match score", async () => {
    const result = await api.getRecommendations(seekerSession, 0, 20);
    expect(result.content.length).toBeGreaterThan(0);
    expect(result.content[0].matchPercent).toBeGreaterThanOrEqual(result.content[1].matchPercent);
    expect(result.content[0].jobId).toBe("job-1");
  });

  it("supports applying through the typed mock client", async () => {
    const application = await api.apply("job-2", { userId: "candidate-1", email: "maya@example.com", role: "JOB_SEEKER", token: "test-token" });
    expect(application.jobId).toBe("job-2");
    expect(application.status).toBe("APPLIED");
  });

  it("supports paginating recruiter candidate rankings", async () => {
    const first = await api.getCandidateRanking("job-1", { ...seekerSession, role: "RECRUITER" }, 0, 1);
    const second = await api.getCandidateRanking("job-1", { ...seekerSession, role: "RECRUITER" }, 1, 1);
    expect(first.content[0].candidateId).toBe("candidate-1");
    expect(second.content[0].candidateId).toBe("candidate-2");
    expect(second.number).toBe(1);
  });
});
