"use client";

import React, { useState } from "react";
import { QuestionResult } from "@/types/test";
import { 
  CheckCircle2, 
  XCircle, 
  MinusCircle, 
  Lightbulb, 
  HelpCircle, 
  ChevronDown, 
  ChevronUp,
  Tag
} from "lucide-react";
import { Badge } from "@/components/ui/Badge";

interface QuestionReviewCardProps {
  question: QuestionResult;
  index: number;
}

const OPTION_LETTERS = ["A", "B", "C", "D"];

export function QuestionReviewCard({ question, index }: QuestionReviewCardProps) {
  const [expanded, setExpanded] = useState<boolean>(true);

  // Helper to render formatted text with embedded code blocks
  const renderFormattedText = (text: string) => {
    if (!text.includes("```")) {
      return <p className="text-xs sm:text-sm font-semibold text-slate-900 leading-relaxed">{text}</p>;
    }

    const parts = text.split(/(```[\s\S]*?```)/g);
    return (
      <div className="space-y-2">
        {parts.map((part, i) => {
          if (part.startsWith("```")) {
            const lines = part.replace(/^```[a-zA-Z]*\n?/, "").replace(/```$/, "");
            return (
              <pre
                key={i}
                className="bg-slate-900 text-slate-100 p-3.5 rounded-xl text-xs font-mono overflow-x-auto border border-slate-800 leading-relaxed shadow-inner"
              >
                <code>{lines}</code>
              </pre>
            );
          }
          if (!part.trim()) return null;
          return (
            <p key={i} className="text-xs sm:text-sm font-semibold text-slate-900 leading-relaxed">
              {part}
            </p>
          );
        })}
      </div>
    );
  };

  return (
    <div className="bg-white border border-slate-200/90 rounded-2xl p-6 shadow-xs space-y-4">
      {/* Header with Result Status */}
      <div className="flex flex-wrap items-center justify-between gap-3 pb-3 border-b border-slate-100">
        <div className="flex items-center gap-2">
          {question.isCorrect ? (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 font-bold text-xs border border-emerald-100">
              <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" />
              <span>Correct</span>
            </span>
          ) : question.isSkipped ? (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-100 text-slate-700 font-bold text-xs border border-slate-200">
              <MinusCircle className="h-3.5 w-3.5 text-slate-400" />
              <span>Skipped</span>
            </span>
          ) : (
            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-rose-50 text-rose-700 font-bold text-xs border border-rose-100">
              <XCircle className="h-3.5 w-3.5 text-rose-600" />
              <span>Not Quite</span>
            </span>
          )}

          <span className="text-xs font-mono font-bold text-slate-500">
            Q{index + 1}
          </span>
        </div>

        <div className="flex items-center gap-2">
          <Badge variant="purple" size="sm">
            <Tag className="h-3 w-3" />
            <span>{question.topic}</span>
          </Badge>
          <Badge variant="neutral" size="sm">
            {question.difficulty}
          </Badge>

          <button
            type="button"
            onClick={() => setExpanded(!expanded)}
            className="text-slate-400 hover:text-slate-600 p-1"
          >
            {expanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
          </button>
        </div>
      </div>

      {/* Question Text */}
      <div>{renderFormattedText(question.question)}</div>

      {expanded && (
        <div className="space-y-4 pt-2">
          {/* Options Breakdown with Candidate vs Correct highlights */}
          <div className="space-y-2">
            {question.options.map((opt, optIdx) => {
              const letter = OPTION_LETTERS[optIdx];
              const isUserChoice = question.userAnswer === opt;
              const isCorrectOpt = question.correctAnswer === opt;

              return (
                <div
                  key={optIdx}
                  className={`p-3 rounded-xl border text-xs flex items-start justify-between gap-3 transition-colors ${
                    isCorrectOpt
                      ? "bg-emerald-50/70 border-emerald-300 text-emerald-950 font-semibold"
                      : isUserChoice && !isCorrectOpt
                      ? "bg-rose-50/70 border-rose-300 text-rose-950"
                      : "bg-slate-50 border-slate-200 text-slate-700"
                  }`}
                >
                  <div className="flex items-start gap-2.5">
                    <span
                      className={`h-5 w-5 rounded-md flex items-center justify-center font-bold text-[10px] shrink-0 ${
                        isCorrectOpt
                          ? "bg-emerald-600 text-white"
                          : isUserChoice && !isCorrectOpt
                          ? "bg-rose-600 text-white"
                          : "bg-slate-200 text-slate-700"
                      }`}
                    >
                      {letter}
                    </span>
                    <span className="leading-relaxed">{opt}</span>
                  </div>

                  <div className="shrink-0 flex items-center gap-1.5 font-bold">
                    {isCorrectOpt && (
                      <span className="inline-flex items-center gap-1 text-emerald-700 text-[11px]">
                        <CheckCircle2 className="h-3.5 w-3.5" />
                        <span>Correct Answer</span>
                      </span>
                    )}
                    {isUserChoice && !isCorrectOpt && (
                      <span className="inline-flex items-center gap-1 text-rose-700 text-[11px]">
                        <XCircle className="h-3.5 w-3.5" />
                        <span>Your Choice</span>
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>

          {/* Detailed Explanations Box (Requirement #31) */}
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 space-y-3 text-xs">
            {/* Why Correct is Correct */}
            <div>
              <span className="font-bold text-emerald-800 flex items-center gap-1 mb-1">
                <CheckCircle2 className="h-3.5 w-3.5 text-emerald-600" />
                <span>Why the correct answer is right:</span>
              </span>
              <p className="text-slate-700 leading-relaxed pl-5">
                {question.explanation}
              </p>
            </div>

            {/* Option-by-Option Breakdown */}
            {question.optionExplanations && Object.keys(question.optionExplanations).length > 0 && (
              <div className="pt-2 border-t border-slate-200/80 space-y-1.5">
                <span className="font-semibold text-slate-700 block mb-1">
                  Analysis of all options:
                </span>
                {Object.entries(question.optionExplanations).map(([optKey, expText]) => (
                  <div key={optKey} className="pl-2 text-slate-600 leading-relaxed flex items-start gap-1.5">
                    <span className="font-mono font-bold text-slate-800 text-[11px] shrink-0">Option {optKey}:</span>
                    <span>{expText}</span>
                  </div>
                ))}
              </div>
            )}

            {/* Interview Tip */}
            {question.interviewTip && (
              <div className="pt-2 border-t border-slate-200/80 flex items-start gap-2 bg-amber-50/60 p-2.5 rounded-lg border border-amber-200/60">
                <Lightbulb className="h-4 w-4 text-amber-600 shrink-0 mt-0.5" />
                <div>
                  <span className="font-bold text-amber-900 block text-[11px]">Interview Insight:</span>
                  <p className="text-amber-800 leading-relaxed text-[11px] mt-0.5">
                    {question.interviewTip}
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
