import { AnonymousDashboard } from "@/components/dashboard/AnonymousDashboard";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Practice Dashboard — PrepForge",
  description: "Anonymous device practice history, test scores, and targeted growth areas.",
};

export default function DashboardPage() {
  return (
    <div className="bg-slate-50/50 min-h-screen">
      <AnonymousDashboard />
    </div>
  );
}
