"use client";

import React from "react";
import { QuestionType } from "@/types/test";
import { Check } from "lucide-react";

interface QuestionTypeSelectorProps {
  value: QuestionType[];
  onChange: (types: QuestionType[]) => void;
}

const QUESTION_TYPES: { type: QuestionType; label: string; desc: string }[] = [
  {
    type: "Conceptual MCQ",
    label: "Conceptual MCQ",
    desc: "Architecture, theoretical contracts, definitions.",
  },
  {
    type: "Output-based",
    label: "Output-based Snippets",
    desc: "Predict exact console / return results of tricky code blocks.",
  },
  {
    type: "Code analysis",
    label: "Code Analysis",
    desc: "Evaluate runtime complexity, memory behavior, concurrency race states.",
  },
  {
    type: "Scenario-based",
    label: "Scenario-based",
    desc: "Production engineering decisions, microservice failure modes.",
  },
  {
    type: "Debugging",
    label: "Debugging & Fixes",
    desc: "Identify sneaky syntax bugs, concurrency deadlocks, N+1 issues.",
  },
  {
    type: "SQL query/result",
    label: "SQL & Query Result",
    desc: "Window functions, join behavior, indexing execution traces.",
  },
  {
    type: "Best-practice",
    label: "Best Practice & Clean Code",
    desc: "Effective Java idioms, immutability, SOLID principles.",
  },
  {
    type: "Interview trick questions",
    label: "Interview Trick Questions",
    desc: "Common pitfalls and misleading corner cases favored by interviewers.",
  },
];

export function QuestionTypeSelector({ value, onChange }: QuestionTypeSelectorProps) {
  const toggleType = (type: QuestionType) => {
    if (value.includes(type)) {
      if (value.length > 1) {
        onChange(value.filter((t) => t !== type));
      }
    } else {
      onChange([...value, type]);
    }
  };

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <div>
          <label className="text-sm font-bold text-slate-900 block">
            Question Types & Formats <span className="text-rose-500">*</span>
          </label>
          <p className="text-xs text-slate-500 mt-0.5">
            Select one or more question varieties to balance theoretical and code evaluation.
          </p>
        </div>
        <button
          type="button"
          onClick={() => onChange(QUESTION_TYPES.map((q) => q.type))}
          className="text-xs text-indigo-600 hover:text-indigo-800 font-medium"
        >
          Select All
        </button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2">
        {QUESTION_TYPES.map((item) => {
          const isSelected = value.includes(item.type);
          return (
            <div
              key={item.type}
              onClick={() => toggleType(item.type)}
              className={`p-3 rounded-xl border text-left cursor-pointer transition-all duration-150 select-none flex items-start justify-between gap-2 ${
                isSelected
                  ? "bg-indigo-50/70 border-indigo-600 shadow-xs ring-1 ring-indigo-600"
                  : "bg-white border-slate-200 hover:border-slate-300 hover:bg-slate-50/50"
              }`}
            >
              <div>
                <span className={`text-xs font-bold block ${isSelected ? "text-indigo-950" : "text-slate-800"}`}>
                  {item.label}
                </span>
                <span className="text-[10px] text-slate-500 line-clamp-1 mt-0.5 block">
                  {item.desc}
                </span>
              </div>

              <div
                className={`h-4 w-4 rounded flex items-center justify-center shrink-0 mt-0.5 border ${
                  isSelected ? "bg-indigo-600 border-indigo-600 text-white" : "border-slate-300 bg-white"
                }`}
              >
                {isSelected && <Check className="h-3 w-3 stroke-[3]" />}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
