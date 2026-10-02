import React from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { describe, expect, it } from "vitest";
import { CandidateRankingList } from "./CandidateRankingList";
import type { CandidateRanking } from "@/lib/types";

const candidate: CandidateRanking = { candidateId: "candidate-123456", matchPercent: 91, semanticScore: 0.84, semanticUsed: true, matchedSkills: ["React"], missingSkills: ["GraphQL"], applicationStatus: "SHORTLISTED" };

describe("CandidateRankingList", () => {
  it("renders ranked candidate details, semantic score, and profile navigation", () => {
    const html = renderToStaticMarkup(<CandidateRankingList candidates={[candidate]} applicationIds={{ "candidate-123456": "application-1" }} onStatusChange={() => undefined} />);
    expect(html).toContain("91% match");
    expect(html).toContain("Semantic 84%");
    expect(html).toContain("React");
    expect(html).toContain("GraphQL");
    expect(html).toContain("/candidates/candidate-123456/profile");
  });

  it("does not display a semantic value when it is unavailable", () => {
    const html = renderToStaticMarkup(<CandidateRankingList candidates={[{ ...candidate, semanticScore: null, semanticUsed: false }]} applicationIds={{ "candidate-123456": "application-1" }} onStatusChange={() => undefined} />);
    expect(html).not.toContain("Semantic");
  });
});
