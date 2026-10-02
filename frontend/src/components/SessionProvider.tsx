"use client";
import { createContext, useContext, useEffect, useState } from "react";
import type { Session } from "@/lib/types";
type Context = { session: Session | null; ready: boolean; signIn: (session: Session) => void; signOut: () => void };
const SessionContext = createContext<Context>({ session: null, ready: false, signIn: () => undefined, signOut: () => undefined });
export function SessionProvider({ children }: { children: React.ReactNode }) { const [session, setSession] = useState<Session | null>(null); const [ready, setReady] = useState(false); useEffect(() => { try { const stored = window.sessionStorage.getItem("jobrec_session"); if (stored) setSession(JSON.parse(stored)); } catch { window.sessionStorage.removeItem("jobrec_session"); } setReady(true); }, []); const signIn = (value: Session) => { setSession(value); window.sessionStorage.setItem("jobrec_session", JSON.stringify(value)); }; const signOut = () => { setSession(null); window.sessionStorage.removeItem("jobrec_session"); }; return <SessionContext.Provider value={{ session, ready, signIn, signOut }}>{children}</SessionContext.Provider>; }
export const useSession = () => useContext(SessionContext);
