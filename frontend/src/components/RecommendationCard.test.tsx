import React from "react";
import { renderToStaticMarkup } from "react-dom/server";
import { describe, expect, it } from "vitest";
import { RecommendationCard } from "./RecommendationCard";
import type { Recommendation } from "@/lib/types";

const recommendation: Recommendation = {
  jobId: "job-test",
  title: "Frontend Engineer",
  company: "Example Labs",
  location: "Remote",
  matchPercent: 92,
  matchedSkills: ["TypeScript", "React"],
  missingSkills: ["Testing"],
  skillGap: {
    missingRequiredSkills: ["Testing"],
    missingPreferredSkills: [],
    coveragePercentage: 67,
    priorityMissingSkills: ["Testing"],
    suggestions: { Testing: "Add Testing to your profile or upload a certificate showing it." },
  },
};

describe("RecommendationCard", () => {
  it("renders the match score and skill groups", () => {
    const html = renderToStaticMarkup(<RecommendationCard item={recommendation} />);
    expect(html).toContain("92% match");
    expect(html).toContain("TypeScript");
    expect(html).toContain("Testing");
    expect(html).toContain("View role");
  });
});
