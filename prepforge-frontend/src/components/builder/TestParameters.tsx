"use client";

import React from "react";
import { Clock, Layers } from "lucide-react";

interface TestParametersProps {
  questionCount: number;
  onQuestionCountChange: (count: number) => void;
  timeLimitMinutes: number;
  onTimeLimitChange: (minutes: number) => void;
}

const COUNT_PRESETS = [5, 10, 15, 20, 30, 40];

export function TestParameters({
  questionCount,
  onQuestionCountChange,
  timeLimitMinutes,
  onTimeLimitChange,
}: TestParametersProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-4 rounded-xl bg-slate-50/80 border border-slate-200/80">
      {/* Question count */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <label className="text-xs font-bold text-slate-900 flex items-center gap-1.5">
            <Layers className="h-3.5 w-3.5 text-indigo-600" />
            <span>Question Count:</span>
            <span className="text-indigo-600 font-mono text-sm font-bold">{questionCount} Qs</span>
          </label>
          <span className="text-[11px] text-slate-400">Guaranteed exact count</span>
        </div>

        <div className="flex items-center gap-1.5">
          {COUNT_PRESETS.map((preset) => (
            <button
              key={preset}
              type="button"
              onClick={() => onQuestionCountChange(preset)}
              className={`flex-1 py-1.5 text-xs font-mono font-semibold rounded-lg border transition-colors ${
                questionCount === preset
                  ? "bg-indigo-600 border-indigo-600 text-white shadow-xs"
                  : "bg-white border-slate-200 text-slate-700 hover:bg-slate-100"
              }`}
            >
              {preset}
            </button>
          ))}
        </div>
      </div>

      {/* Timer options */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <label className="text-xs font-bold text-slate-900 flex items-center gap-1.5">
            <Clock className="h-3.5 w-3.5 text-indigo-600" />
            <span>Time Limit:</span>
            <span className="text-indigo-600 font-mono text-sm font-bold">
              {timeLimitMinutes > 0 ? `${timeLimitMinutes} mins` : "Untimed"}
            </span>
          </label>
          <span className="text-[11px] text-slate-400">
            {timeLimitMinutes > 0 ? `~${Math.round((timeLimitMinutes * 60) / questionCount)}s / question` : "Self-paced"}
          </span>
        </div>

        <div className="flex items-center gap-1.5">
          {[
            { label: "10m", val: 10 },
            { label: "15m", val: 15 },
            { label: "25m", val: 25 },
            { label: "35m", val: 35 },
            { label: "50m", val: 50 },
            { label: "None", val: 0 },
          ].map((item) => (
            <button
              key={item.val}
              type="button"
              onClick={() => onTimeLimitChange(item.val)}
              className={`flex-1 py-1.5 text-xs font-mono font-semibold rounded-lg border transition-colors ${
                timeLimitMinutes === item.val
                  ? "bg-indigo-600 border-indigo-600 text-white shadow-xs"
                  : "bg-white border-slate-200 text-slate-700 hover:bg-slate-100"
              }`}
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
