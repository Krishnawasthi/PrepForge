"use client";

import React, { useEffect, useState } from "react";
import { Clock, AlertTriangle, Send } from "lucide-react";
import { Button } from "@/components/ui/Button";

interface TestHeaderProps {
  title: string;
  currentIndex: number;
  totalQuestions: number;
  timeLimitMinutes: number;
  onTimeExpire: () => void;
  onSubmitClick: () => void;
  isSubmitting: boolean;
}

export function TestHeader({
  title,
  currentIndex,
  totalQuestions,
  timeLimitMinutes,
  onTimeExpire,
  onSubmitClick,
  isSubmitting,
}: TestHeaderProps) {
  const [secondsRemaining, setSecondsRemaining] = useState<number>(
    timeLimitMinutes > 0 ? timeLimitMinutes * 60 : 0
  );

  useEffect(() => {
    if (timeLimitMinutes <= 0) return;

    const interval = setInterval(() => {
      setSecondsRemaining((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          onTimeExpire();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [timeLimitMinutes, onTimeExpire]);

  const mins = Math.floor(secondsRemaining / 60);
  const secs = secondsRemaining % 60;
  const isUrgent = timeLimitMinutes > 0 && secondsRemaining < 120; // under 2 minutes
  const progressPercent = ((currentIndex + 1) / totalQuestions) * 100;

  return (
    <header className="sticky top-0 z-40 bg-white border-b border-slate-200 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          {/* Title & Question Counter */}
          <div>
            <span className="text-[11px] font-semibold uppercase tracking-wider text-indigo-600 block">
              {title}
            </span>
            <div className="flex items-center gap-2 mt-0.5">
              <span className="text-sm font-extrabold text-slate-900 font-mono">
                Question {currentIndex + 1} of {totalQuestions}
              </span>
              <span className="text-xs text-slate-400">({Math.round(progressPercent)}%)</span>
            </div>
          </div>

          {/* Timer & Submit CTA */}
          <div className="flex items-center justify-between sm:justify-end gap-3">
            {timeLimitMinutes > 0 && (
              <div
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg border text-xs font-mono font-bold transition-colors ${
                  isUrgent
                    ? "bg-rose-50 border-rose-300 text-rose-700 animate-pulse"
                    : "bg-slate-50 border-slate-200 text-slate-800"
                }`}
                title="Remaining Time"
              >
                {isUrgent ? (
                  <AlertTriangle className="h-3.5 w-3.5 text-rose-600" />
                ) : (
                  <Clock className="h-3.5 w-3.5 text-indigo-600" />
                )}
                <span>
                  {mins.toString().padStart(2, "0")}:{secs.toString().padStart(2, "0")}
                </span>
              </div>
            )}

            <Button
              variant="primary"
              size="sm"
              onClick={onSubmitClick}
              isLoading={isSubmitting}
              className="bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500 shadow-sm text-xs"
            >
              <Send className="h-3.5 w-3.5" />
              <span>Submit Assessment</span>
            </Button>
          </div>
        </div>

        {/* Linear Progress Bar */}
        <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-3">
          <div
            className="bg-indigo-600 h-full rounded-full transition-all duration-300 ease-out"
            style={{ width: `${progressPercent}%` }}
          />
        </div>
      </div>
    </header>
  );
}
