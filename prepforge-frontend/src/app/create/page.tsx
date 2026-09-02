import { TestBuilder } from "@/components/builder/TestBuilder";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Create Assessment — PrepForge",
  description: "Configure and generate a personalized technical interview test with AI.",
};

export default function CreateTestPage() {
  return (
    <div className="py-12 bg-slate-50/50 min-h-screen">
      <TestBuilder />
    </div>
  );
}
