"use client";

import React from "react";
import { AlertTriangle, CheckCircle2, X } from "lucide-react";
import { Button } from "@/components/ui/Button";

interface SubmissionConfirmModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  totalQuestions: number;
  answeredCount: number;
  unansweredCount: number;
  isSubmitting: boolean;
}

export function SubmissionConfirmModal({
  isOpen,
  onClose,
  onConfirm,
  totalQuestions,
  answeredCount,
  unansweredCount,
  isSubmitting,
}: SubmissionConfirmModalProps) {
  if (!isOpen) return null;

  const hasUnanswered = unansweredCount > 0;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
      <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200 space-y-5">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-3">
            <div
              className={`h-10 w-10 rounded-xl flex items-center justify-center shrink-0 ${
                hasUnanswered ? "bg-amber-50 text-amber-600" : "bg-emerald-50 text-emerald-600"
              }`}
            >
              {hasUnanswered ? (
                <AlertTriangle className="h-5 w-5" />
              ) : (
                <CheckCircle2 className="h-5 w-5" />
              )}
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">
                {hasUnanswered ? "Unanswered Questions Detected" : "Ready to Submit Assessment?"}
              </h3>
              <p className="text-xs text-slate-500 mt-0.5">
                {hasUnanswered
                  ? `You have answered ${answeredCount} of ${totalQuestions} questions.`
                  : "All questions have been answered."}
              </p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="text-slate-400 hover:text-slate-700 p-1"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        {hasUnanswered && (
          <div className="p-3.5 rounded-xl bg-amber-50 border border-amber-200 text-xs text-amber-900 leading-relaxed">
            <p className="font-semibold text-amber-800">
              You still have {unansweredCount} unanswered question{unansweredCount > 1 ? "s" : ""}.
            </p>
            <p className="text-amber-700 mt-1">
              Unanswered questions will be scored as skipped. You can review your answers or submit now.
            </p>
          </div>
        )}

        <div className="flex items-center justify-end gap-3 pt-2">
          <Button
            variant="outline"
            size="md"
            onClick={onClose}
            disabled={isSubmitting}
            className="text-xs"
          >
            Review Answers
          </Button>

          <Button
            variant="primary"
            size="md"
            onClick={onConfirm}
            isLoading={isSubmitting}
            className="bg-emerald-600 hover:bg-emerald-700 focus:ring-emerald-500 text-xs"
          >
            Submit Assessment
          </Button>
        </div>
      </div>
    </div>
  );
}
