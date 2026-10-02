"use client";

import { useEffect, useState, useRef } from "react";
import Link from "next/link";
import { Bell, Check, CheckCircle2 } from "lucide-react";
import { api } from "@/lib/api";
import { useSession } from "./SessionProvider";
import type { Notification } from "@/lib/types";
import { getUnreadCount, markAsReadInState, markAllAsReadInState } from "@/lib/notifications";
import { useRouter } from "next/navigation";

export function NotificationsDropdown() {
  const { session } = useSession();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const router = useRouter();

  useEffect(() => {
    const fetchNotifications = async () => {
      if (!session) return;
      try {
        const data = await api.getNotifications(session);
        setNotifications(data);
        setError(null);
      } catch (err) {
        setError("Failed to load notifications.");
      }
    };
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 60000);
    return () => clearInterval(interval);
  }, [session]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const unreadCount = getUnreadCount(notifications);

  const markAsRead = async (id: string, e?: React.MouseEvent) => {
    if (e) {
      e.stopPropagation();
      e.preventDefault();
    }
    if (!session) return;
    try {
      await api.markNotificationRead(id, session);
      setNotifications((prev) => markAsReadInState(prev, id));
    } catch (err) {
      console.error(err);
    }
  };

  const markAllAsRead = async () => {
    if (!session) return;
    try {
      await api.markAllNotificationsRead(session);
      setNotifications((prev) => markAllAsReadInState(prev));
    } catch (err) {
      console.error(err);
    }
  };

  const handleNotificationClick = async (n: Notification) => {
    if (!n.read) {
      await markAsRead(n.id);
    }
    setIsOpen(false);
    if (n.refId) {
      if (n.type === "RECOMMENDED_JOB") {
        router.push(`/jobs/${n.refId}`);
      } else if (n.type === "APPLICATION_STATUS_CHANGED") {
        if (session?.role === "RECRUITER") {
          router.push(`/recruiter/jobs/${n.refId}`);
        } else {
          router.push(`/applications`);
        }
      } else {
        router.push(`/`);
      }
    }
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 text-slate-500 hover:bg-slate-100 hover:text-ink rounded-full transition-colors"
        aria-label="Notifications"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute top-1 right-1 grid h-4 w-4 place-items-center rounded-full bg-coral text-[10px] font-bold text-white">
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 max-w-[calc(100vw-2rem)] origin-top-right rounded-2xl bg-white shadow-xl ring-1 ring-slate-900/5 z-50">
          <div className="flex items-center justify-between border-b border-slate-100 p-4">
            <h3 className="font-bold text-ink">Notifications</h3>
            {unreadCount > 0 && (
              <button
                onClick={markAllAsRead}
                className="text-xs font-semibold text-coral hover:text-coral/80"
              >
                Mark all read
              </button>
            )}
          </div>

          <div className="max-h-96 overflow-y-auto">
            {error ? (
              <div className="p-8 text-center text-sm text-slate-500">{error}</div>
            ) : notifications.length === 0 ? (
              <div className="p-8 text-center">
                <Bell className="mx-auto mb-2 h-8 w-8 text-slate-300" />
                <p className="text-sm text-slate-500">No notifications yet.</p>
              </div>
            ) : (
              <div className="divide-y divide-slate-100">
                {notifications.map((n) => (
                  <div
                    key={n.id}
                    onClick={() => handleNotificationClick(n)}
                    className={`block p-4 transition-colors hover:bg-slate-50 cursor-pointer ${
                      !n.read ? "bg-blue-50/50" : ""
                    }`}
                  >
                    <div className="flex gap-3">
                      <div className="flex-1 space-y-1">
                        <p
                          className={`text-sm ${
                            !n.read ? "font-bold text-ink" : "text-slate-600"
                          }`}
                        >
                          {n.title}
                        </p>
                        <p className="text-xs text-slate-500">{n.message}</p>
                        <p className="text-[10px] text-slate-400">
                          {new Date(n.createdAt).toLocaleString()}
                        </p>
                      </div>
                      {!n.read && (
                        <button
                          onClick={(e) => markAsRead(n.id, e)}
                          className="self-start p-1 text-slate-400 hover:text-coral"
                          title="Mark as read"
                        >
                          <CheckCircle2 className="h-4 w-4" />
                        </button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
