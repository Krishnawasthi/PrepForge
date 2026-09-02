"use client";

import React from "react";
import { cn } from "@/lib/utils";
import { LayoutGrid } from "lucide-react";

interface QuestionNavigatorProps {
  totalQuestions: number;
  currentIndex: number;
  answers: Record<string, string>;
  questionIds: string[];
  markedForReview: Set<number>;
  onSelectQuestion: (index: number) => void;
}

export function QuestionNavigator({
  totalQuestions,
  currentIndex,
  answers,
  questionIds,
  markedForReview,
  onSelectQuestion,
}: QuestionNavigatorProps) {
  const answeredCount = Object.keys(answers).filter((k) => answers[k] && answers[k].trim()).length;
  const reviewCount = markedForReview.size;
  const unansweredCount = totalQuestions - answeredCount;

  return (
    <div className="bg-white border border-slate-200/90 rounded-2xl shadow-xs p-5 space-y-4">
      <div className="flex items-center justify-between pb-3 border-b border-slate-100">
        <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700 flex items-center gap-1.5">
          <LayoutGrid className="h-3.5 w-3.5 text-indigo-600" />
          <span>Question Navigator</span>
        </h3>
        <span className="text-[11px] text-slate-400 font-mono">
          {answeredCount}/{totalQuestions} Answered
        </span>
      </div>

      {/* Legend */}
      <div className="grid grid-cols-2 gap-2 text-[11px] text-slate-600 pb-2">
        <div className="flex items-center gap-1.5">
          <span className="h-2.5 w-2.5 rounded-full bg-emerald-500 inline-block"></span>
          <span>Answered ({answeredCount})</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="h-2.5 w-2.5 rounded-full bg-amber-400 inline-block"></span>
          <span>Review ({reviewCount})</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="h-2.5 w-2.5 rounded-full bg-indigo-600 inline-block"></span>
          <span>Current</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="h-2.5 w-2.5 rounded-full bg-slate-200 inline-block"></span>
          <span>Unanswered ({unansweredCount})</span>
        </div>
      </div>

      {/* Grid of Question Number Buttons */}
      <div className="grid grid-cols-5 sm:grid-cols-6 lg:grid-cols-5 gap-2 max-h-72 overflow-y-auto pr-1">
        {Array.from({ length: totalQuestions }).map((_, idx) => {
          const qId = questionIds[idx];
          const isCurrent = idx === currentIndex;
          const isAnswered = qId && answers[qId] && answers[qId].trim();
          const isMarked = markedForReview.has(idx);

          return (
            <button
              key={idx}
              type="button"
              onClick={() => onSelectQuestion(idx)}
              className={cn(
                "h-9 w-full rounded-xl text-xs font-bold font-mono transition-all flex items-center justify-center relative border",
                isCurrent && "border-indigo-600 ring-2 ring-indigo-500 ring-offset-1 z-10",
                !isCurrent && isAnswered && "bg-emerald-50 text-emerald-800 border-emerald-300 hover:bg-emerald-100",
                !isCurrent && !isAnswered && "bg-slate-50 text-slate-700 border-slate-200 hover:bg-slate-100",
                isCurrent && isAnswered && "bg-indigo-600 text-white",
                isCurrent && !isAnswered && "bg-indigo-600 text-white"
              )}
            >
              {idx + 1}
              {isMarked && (
                <span className="absolute -top-1 -right-1 h-2.5 w-2.5 rounded-full bg-amber-500 border border-white"></span>
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
}
