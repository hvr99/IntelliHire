import { describe, expect, it } from "vitest";
import { validateDocumentFile } from "./DocumentUpload";
import { toggleSuggestionSelection } from "./SkillSuggestions";

describe("document skill workflow", () => {
  it("validates supported files, empty files, and size limits", () => {
    expect(validateDocumentFile(new File(["resume"], "resume.pdf", { type: "application/pdf" }), "resume", 100)).toBeUndefined();
    expect(validateDocumentFile(new File([], "resume.pdf", { type: "application/pdf" }), "resume", 100)).toContain("empty");
    expect(validateDocumentFile(new File(["x"], "resume.exe", { type: "application/octet-stream" }), "resume", 100)).toContain("Unsupported");
    expect(validateDocumentFile(new File(["large"], "certificate.png", { type: "image/png" }), "certificate", 2)).toContain("smaller");
  });

  it("selects and deselects suggestions for the confirmation payload", () => {
    const selected = toggleSuggestionSelection(new Set(["one", "two"]), "two");
    expect([...selected]).toEqual(["one"]);
    expect([...toggleSuggestionSelection(selected, "three")]).toEqual(["one", "three"]);
  });
});
