import { describe, expect, it } from "vitest";
import { getRoleRedirect } from "./RoleGuard";

describe("route protection", () => {
  it("redirects unauthenticated users to login", () => {
    expect(getRoleRedirect(null, true)).toBe("/login");
  });

  it("redirects a seeker away from recruiter routes", () => {
    expect(getRoleRedirect({ userId: "u1", name: "Alex", email: "alex@example.com", role: "JOB_SEEKER" }, true, "RECRUITER")).toBe("/dashboard");
  });

  it("allows the requested role", () => {
    expect(getRoleRedirect({ userId: "r1", name: "Sam", email: "sam@example.com", role: "RECRUITER" }, true, "RECRUITER")).toBeNull();
  });
});
