"use client";

import React from "react";
import { ExperienceLevel } from "@/types/test";
import { UserCheck, Sparkles, Award, ShieldCheck, Briefcase } from "lucide-react";

interface ExperienceSelectorProps {
  value: ExperienceLevel;
  onChange: (level: ExperienceLevel) => void;
}

const EXPERIENCE_OPTIONS: { level: ExperienceLevel; label: string; desc: string; icon: React.ComponentType<{ className?: string }> }[] = [
  {
    level: "Beginner",
    label: "Beginner / Fresher",
    desc: "Foundational syntax, core OOP concepts, basic data types.",
    icon: Sparkles,
  },
  {
    level: "0-1 years",
    label: "0–1 Years",
    desc: "Junior role readiness, fundamental collections, simple queries.",
    icon: UserCheck,
  },
  {
    level: "1-2 years",
    label: "1–2 Years",
    desc: "Intermediate concepts, Stream pipelines, exception handling.",
    icon: Briefcase,
  },
  {
    level: "2-3 years",
    label: "2–3 Years",
    desc: "Multithreading, Spring Boot annotations, JPA relationships, caching.",
    icon: Award,
  },
  {
    level: "3-5 years",
    label: "3–5 Years",
    desc: "Advanced concurrency, JVM tuning, system design edge cases, ACID.",
    icon: ShieldCheck,
  },
  {
    level: "5+ years",
    label: "5+ Years (Senior / Lead)",
    desc: "Architectural trade-offs, internal deep dives, distributed systems.",
    icon: Award,
  },
];

export function ExperienceSelector({ value, onChange }: ExperienceSelectorProps) {
  return (
    <div className="space-y-3">
      <div>
        <label className="text-sm font-bold text-slate-900 block">
          Experience Level <span className="text-rose-500">*</span>
        </label>
        <p className="text-xs text-slate-500 mt-0.5">
          Shapes the depth of questions, code snippet complexity, and real-world scenario expectations.
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2.5">
        {EXPERIENCE_OPTIONS.map((item) => {
          const isSelected = value === item.level;
          const Icon = item.icon;
          return (
            <div
              key={item.level}
              onClick={() => onChange(item.level)}
              className={`p-3.5 rounded-xl border text-left cursor-pointer transition-all duration-150 select-none ${
                isSelected
                  ? "bg-indigo-50/70 border-indigo-600 shadow-xs ring-1 ring-indigo-600"
                  : "bg-white border-slate-200 hover:border-slate-300 hover:bg-slate-50/50"
              }`}
            >
              <div className="flex items-center gap-2 mb-1">
                <div className={`h-6 w-6 rounded-md flex items-center justify-center ${
                  isSelected ? "bg-indigo-600 text-white" : "bg-slate-100 text-slate-600"
                }`}>
                  <Icon className="h-3.5 w-3.5" />
                </div>
                <span className={`text-xs font-bold ${isSelected ? "text-indigo-950" : "text-slate-800"}`}>
                  {item.label}
                </span>
              </div>
              <p className="text-[11px] text-slate-500 leading-relaxed">
                {item.desc}
              </p>
            </div>
          );
        })}
      </div>
    </div>
  );
}
