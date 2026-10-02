import { RoleGuard } from "@/components/RoleGuard";
export default function RecruiterLayout({ children }: { children: React.ReactNode }) { return <RoleGuard role="RECRUITER">{children}</RoleGuard>; }
