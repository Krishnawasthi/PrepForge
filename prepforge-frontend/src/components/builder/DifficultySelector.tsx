"use client";

import React from "react";
import { DifficultyLevel } from "@/types/test";
import { Flame, Gauge, Shuffle, Wand2, Compass } from "lucide-react";

interface DifficultySelectorProps {
  value: DifficultyLevel;
  onChange: (difficulty: DifficultyLevel) => void;
}

const DIFFICULTY_OPTIONS: { level: DifficultyLevel; label: string; desc: string; icon: React.ComponentType<{ className?: string }>; color: string }[] = [
  {
    level: "Easy",
    label: "Easy",
    desc: "Clear direct definitions and standard syntax verification.",
    icon: Compass,
    color: "text-emerald-600 bg-emerald-50",
  },
  {
    level: "Medium",
    label: "Medium",
    desc: "Standard industry interview baseline with standard scenario traps.",
    icon: Gauge,
    color: "text-blue-600 bg-blue-50",
  },
  {
    level: "Hard",
    label: "Hard",
    desc: "Deep internals, multi-layered code traces, and tricky edge cases.",
    icon: Flame,
    color: "text-rose-600 bg-rose-50",
  },
  {
    level: "Mixed",
    label: "Mixed (Balanced)",
    desc: "30% Easy, 50% Medium, 20% Hard for a realistic interview curve.",
    icon: Shuffle,
    color: "text-purple-600 bg-purple-50",
  },
  {
    level: "Adaptive",
    label: "Adaptive Mode",
    desc: "Dynamically escalates or recalibrates based on ongoing test pacing.",
    icon: Wand2,
    color: "text-amber-600 bg-amber-50",
  },
];

export function DifficultySelector({ value, onChange }: DifficultySelectorProps) {
  return (
    <div className="space-y-3">
      <div>
        <label className="text-sm font-bold text-slate-900 block">
          Difficulty Level <span className="text-rose-500">*</span>
        </label>
        <p className="text-xs text-slate-500 mt-0.5">
          Select test rigor or choose Mixed for a comprehensive assessment distribution.
        </p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2.5">
        {DIFFICULTY_OPTIONS.map((item) => {
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
                <div className={`h-6 w-6 rounded-md flex items-center justify-center ${item.color}`}>
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
