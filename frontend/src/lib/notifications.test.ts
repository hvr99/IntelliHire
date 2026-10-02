import { describe, it, expect } from "vitest";
import { getUnreadCount, markAsReadInState, markAllAsReadInState } from "./notifications";
import type { Notification } from "./types";

describe("notifications logic", () => {
  const mockNotifications: Notification[] = [
    { id: "1", userId: "u1", type: "RECOMMENDED_JOB", title: "T1", message: "M1", refId: null, read: false, createdAt: "" },
    { id: "2", userId: "u1", type: "APPLICATION_STATUS_CHANGED", title: "T2", message: "M2", refId: null, read: true, createdAt: "" },
  ];

  it("calculates unread count", () => {
    expect(getUnreadCount(mockNotifications)).toBe(1);
    expect(getUnreadCount([])).toBe(0);
  });

  it("marks a notification as read", () => {
    const updated = markAsReadInState(mockNotifications, "1");
    expect(updated.find(n => n.id === "1")?.read).toBe(true);
    expect(getUnreadCount(updated)).toBe(0);
  });

  it("marks all notifications as read", () => {
    const updated = markAllAsReadInState(mockNotifications);
    expect(updated.every(n => n.read)).toBe(true);
  });
});
