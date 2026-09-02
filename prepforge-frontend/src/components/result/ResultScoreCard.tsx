"use client";

import React from "react";
import { CheckCircle2, XCircle, MinusCircle, Clock, Trophy, Sparkles } from "lucide-react";
import { formatTime } from "@/lib/utils";

interface ResultScoreCardProps {
  score: number;
  totalQuestions: number;
  percentage: number;
  correctCount: number;
  incorrectCount: number;
  skippedCount: number;
  timeTakenSeconds: number;
  feedbackMessage: string;
  testTitle: string;
}

export function ResultScoreCard({
  score,
  totalQuestions,
  percentage,
  correctCount,
  incorrectCount,
  skippedCount,
  timeTakenSeconds,
  feedbackMessage,
  testTitle,
}: ResultScoreCardProps) {
  const isHighPass = percentage >= 80;
  const isPass = percentage >= 60;

  return (
    <div className="bg-white border border-slate-200/90 rounded-2xl p-6 sm:p-8 shadow-sm space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-100">
        <div>
          <span className="text-xs font-semibold uppercase tracking-wider text-indigo-600">
            Assessment Results
          </span>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight mt-0.5">
            {testTitle}
          </h1>
        </div>

        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-xs font-mono text-slate-700">
            <Clock className="h-3.5 w-3.5 text-indigo-600" />
            <span>Time: {formatTime(timeTakenSeconds)}</span>
          </div>
        </div>
      </div>

      {/* Main Score Hero */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-center">
        {/* Large Percentage */}
        <div className="flex items-center gap-4 p-4 rounded-xl bg-gradient-to-br from-indigo-50/70 to-slate-50 border border-indigo-100">
          <div className="h-16 w-16 rounded-2xl bg-indigo-600 text-white flex items-center justify-center font-mono font-black text-xl shadow-md shadow-indigo-200">
            {Math.round(percentage)}%
          </div>
          <div>
            <span className="text-xs text-slate-500 font-medium block">Final Score</span>
            <span className="text-2xl font-extrabold text-slate-900 font-mono">
              {Math.round(score)} <span className="text-sm font-medium text-slate-400">/ {totalQuestions}</span>
            </span>
          </div>
        </div>

        {/* Breakdown Counts */}
        <div className="md:col-span-2 grid grid-cols-3 gap-3">
          <div className="p-3.5 rounded-xl bg-emerald-50/80 border border-emerald-100 flex items-center gap-3">
            <CheckCircle2 className="h-5 w-5 text-emerald-600 shrink-0" />
            <div>
              <span className="text-[11px] text-emerald-700 font-medium block">Correct</span>
              <span className="text-lg font-bold text-emerald-950 font-mono">{correctCount}</span>
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-rose-50/80 border border-rose-100 flex items-center gap-3">
            <XCircle className="h-5 w-5 text-rose-600 shrink-0" />
            <div>
              <span className="text-[11px] text-rose-700 font-medium block">Incorrect</span>
              <span className="text-lg font-bold text-rose-950 font-mono">{incorrectCount}</span>
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-200 flex items-center gap-3">
            <MinusCircle className="h-5 w-5 text-slate-400 shrink-0" />
            <div>
              <span className="text-[11px] text-slate-600 font-medium block">Skipped</span>
              <span className="text-lg font-bold text-slate-900 font-mono">{skippedCount}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Optimistic Feedback Banner (Requirement #30) */}
      <div className="p-4 rounded-xl bg-indigo-50/70 border border-indigo-100 flex items-start gap-3">
        <Sparkles className="h-5 w-5 text-indigo-600 shrink-0 mt-0.5" />
        <p className="text-xs sm:text-sm font-medium text-indigo-950 leading-relaxed">
          {feedbackMessage}
        </p>
      </div>
    </div>
  );
}
