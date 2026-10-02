import type { Notification } from "./types";

export function getUnreadCount(notifications: Notification[]): number {
  return notifications.filter((n) => !n.read).length;
}

export function markAsReadInState(notifications: Notification[], id: string): Notification[] {
  return notifications.map((n) => (n.id === id ? { ...n, read: true } : n));
}

export function markAllAsReadInState(notifications: Notification[]): Notification[] {
  return notifications.map((n) => ({ ...n, read: true }));
}
