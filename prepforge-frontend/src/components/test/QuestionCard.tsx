"use client";

import React from "react";
import { Question } from "@/types/test";
import { AnswerOption } from "./AnswerOption";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { 
  ChevronLeft, 
  ChevronRight, 
  RotateCcw, 
  Bookmark, 
  BookmarkCheck,
  Tag
} from "lucide-react";

interface QuestionCardProps {
  question: Question;
  index: number;
  total: number;
  selectedAnswer: string | undefined;
  onSelectAnswer: (answer: string) => void;
  onClearAnswer: () => void;
  isMarkedForReview: boolean;
  onToggleMarkReview: () => void;
  onPrevious: () => void;
  onNext: () => void;
  hasPrevious: boolean;
  hasNext: boolean;
}

export function QuestionCard({
  question,
  index,
  total,
  selectedAnswer,
  onSelectAnswer,
  onClearAnswer,
  isMarkedForReview,
  onToggleMarkReview,
  onPrevious,
  onNext,
  hasPrevious,
  hasNext,
}: QuestionCardProps) {
  // Helper to render formatted question text with embedded code blocks
  const renderFormattedQuestion = (text: string) => {
    if (!text.includes("```")) {
      return <p className="text-sm sm:text-base font-semibold text-slate-900 leading-relaxed">{text}</p>;
    }

    const parts = text.split(/(```[\s\S]*?```)/g);
    return (
      <div className="space-y-3">
        {parts.map((part, i) => {
          if (part.startsWith("```")) {
            const lines = part.replace(/^```[a-zA-Z]*\n?/, "").replace(/```$/, "");
            return (
              <pre
                key={i}
                className="bg-slate-900 text-slate-100 p-4 rounded-xl text-xs sm:text-sm font-mono overflow-x-auto border border-slate-800 leading-relaxed shadow-inner"
              >
                <code>{lines}</code>
              </pre>
            );
          }
          if (!part.trim()) return null;
          return (
            <p key={i} className="text-sm sm:text-base font-semibold text-slate-900 leading-relaxed">
              {part}
            </p>
          );
        })}
      </div>
    );
  };

  return (
    <div className="bg-white border border-slate-200/90 rounded-2xl shadow-sm p-6 sm:p-8 space-y-6">
      {/* Top Metadata Badges */}
      <div className="flex flex-wrap items-center justify-between gap-2 pb-4 border-b border-slate-100">
        <div className="flex flex-wrap items-center gap-2">
          <Badge variant="purple" size="sm">
            <Tag className="h-3 w-3" />
            <span>{question.topic}</span>
          </Badge>
          <Badge variant="neutral" size="sm">
            {question.difficulty}
          </Badge>
          <Badge variant="info" size="sm">
            {question.questionType}
          </Badge>
        </div>

        {/* Mark for Review Button */}
        <button
          type="button"
          onClick={onToggleMarkReview}
          className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors border ${
            isMarkedForReview
              ? "bg-amber-50 border-amber-300 text-amber-800"
              : "bg-slate-50 border-slate-200 text-slate-600 hover:bg-slate-100"
          }`}
        >
          {isMarkedForReview ? (
            <>
              <BookmarkCheck className="h-3.5 w-3.5 text-amber-600" />
              <span>Marked for Review</span>
            </>
          ) : (
            <>
              <Bookmark className="h-3.5 w-3.5 text-slate-400" />
              <span>Mark for Review</span>
            </>
          )}
        </button>
      </div>

      {/* Question Statement */}
      <div className="space-y-4">
        <span className="text-xs font-bold text-slate-400 uppercase tracking-wider block">
          Question {index + 1}
        </span>
        {renderFormattedQuestion(question.question)}
      </div>

      {/* 4 Options Grid */}
      <div className="space-y-3 pt-2">
        {question.options.map((optionText, optIndex) => (
          <AnswerOption
            key={optIndex}
            index={optIndex}
            text={optionText}
            isSelected={selectedAnswer === optionText}
            onSelect={() => onSelectAnswer(optionText)}
          />
        ))}
      </div>

      {/* Bottom Controls */}
      <div className="pt-6 border-t border-slate-100 flex flex-col sm:flex-row items-center justify-between gap-4">
        {/* Clear selection */}
        <div>
          {selectedAnswer && (
            <button
              type="button"
              onClick={onClearAnswer}
              className="inline-flex items-center gap-1 text-xs text-slate-500 hover:text-rose-600 font-medium transition-colors"
            >
              <RotateCcw className="h-3 w-3" />
              <span>Clear Choice</span>
            </button>
          )}
        </div>

        {/* Navigation Buttons */}
        <div className="flex items-center gap-3 w-full sm:w-auto">
          <Button
            variant="outline"
            size="md"
            onClick={onPrevious}
            disabled={!hasPrevious}
            className="flex-1 sm:flex-none"
          >
            <ChevronLeft className="h-4 w-4" />
            <span>Previous</span>
          </Button>

          <Button
            variant="primary"
            size="md"
            onClick={onNext}
            disabled={!hasNext}
            className="flex-1 sm:flex-none"
          >
            <span>Next Question</span>
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}
