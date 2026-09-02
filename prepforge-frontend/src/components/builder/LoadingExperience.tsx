"use client";

import React, { useEffect, useState } from "react";
import { Sparkles, Terminal, CheckCircle2 } from "lucide-react";

interface LoadingExperienceProps {
  title?: string;
}

const LOADING_STAGES = [
  "Understanding your requirements...",
  "Selecting the right topics and subtopics...",
  "Building your questions & code snippets...",
  "Balancing difficulty and cognitive depth...",
  "Checking question quality and 4-option integrity...",
  "Preparing your test interface...",
];

export function LoadingExperience({ title = "Generating Your Assessment" }: LoadingExperienceProps) {
  const [currentStage, setCurrentStage] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentStage((prev) => (prev < LOADING_STAGES.length - 1 ? prev + 1 : prev));
    }, 1800);

    return () => clearInterval(interval);
  }, []);

  return (
    <div className="max-w-lg mx-auto p-8 bg-white border border-slate-200/90 rounded-2xl shadow-xl text-center space-y-6">
      <div className="relative mx-auto w-16 h-16 flex items-center justify-center">
        <div className="absolute inset-0 rounded-full border-4 border-indigo-100 animate-ping opacity-30"></div>
        <div className="absolute inset-0 rounded-full border-4 border-indigo-600 border-t-transparent animate-spin"></div>
        <div className="h-10 w-10 rounded-full bg-indigo-50 flex items-center justify-center text-indigo-600">
          <Sparkles className="h-5 w-5 animate-pulse" />
        </div>
      </div>

      <div>
        <h3 className="text-lg font-bold text-slate-900 tracking-tight">{title}</h3>
        <p className="text-xs text-indigo-600 font-semibold mt-1 animate-pulse">
          {LOADING_STAGES[currentStage]}
        </p>
      </div>

      {/* Progressive Stage Indicators */}
      <div className="space-y-2 text-left pt-2 border-t border-slate-100">
        {LOADING_STAGES.map((stage, idx) => {
          const isDone = idx < currentStage;
          const isCurrent = idx === currentStage;
          return (
            <div
              key={idx}
              className={`flex items-center gap-2.5 text-xs transition-opacity duration-300 ${
                isDone ? "text-emerald-700 font-medium" : isCurrent ? "text-indigo-700 font-semibold" : "text-slate-300"
              }`}
            >
              <div className={`h-4 w-4 rounded-full flex items-center justify-center shrink-0 text-[10px] ${
                isDone ? "bg-emerald-100 text-emerald-700" : isCurrent ? "bg-indigo-100 text-indigo-700 animate-pulse" : "bg-slate-100 text-slate-300"
              }`}>
                {isDone ? <CheckCircle2 className="h-3 w-3 text-emerald-600" /> : idx + 1}
              </div>
              <span className="truncate">{stage}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
