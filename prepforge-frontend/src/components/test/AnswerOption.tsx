"use client";

import React from "react";
import { cn } from "@/lib/utils";

interface AnswerOptionProps {
  index: number; // 0, 1, 2, 3 -> A, B, C, D
  text: string;
  isSelected: boolean;
  onSelect: () => void;
  disabled?: boolean;
}

const OPTION_LETTERS = ["A", "B", "C", "D"];

export function AnswerOption({
  index,
  text,
  isSelected,
  onSelect,
  disabled = false,
}: AnswerOptionProps) {
  const letter = OPTION_LETTERS[index] || `${index + 1}`;

  return (
    <div
      onClick={() => {
        if (!disabled) onSelect();
      }}
      role="radio"
      aria-checked={isSelected}
      tabIndex={0}
      onKeyDown={(e) => {
        if ((e.key === "Enter" || e.key === " ") && !disabled) {
          e.preventDefault();
          onSelect();
        }
      }}
      className={cn(
        "flex items-start gap-3.5 p-4 rounded-xl border text-left cursor-pointer transition-all duration-150 select-none",
        isSelected
          ? "bg-indigo-50/80 border-indigo-600 shadow-sm ring-1 ring-indigo-600"
          : "bg-white border-slate-200 hover:border-slate-300 hover:bg-slate-50/60",
        disabled && "opacity-60 cursor-not-allowed"
      )}
    >
      {/* Option Letter indicator */}
      <div
        className={cn(
          "h-7 w-7 rounded-lg flex items-center justify-center font-bold text-xs shrink-0 transition-colors",
          isSelected
            ? "bg-indigo-600 text-white shadow-xs"
            : "bg-slate-100 text-slate-700 border border-slate-200"
        )}
      >
        {letter}
      </div>

      {/* Option Text */}
      <div className="flex-1 pt-0.5">
        <p
          className={cn(
            "text-xs sm:text-sm leading-relaxed",
            isSelected ? "font-semibold text-indigo-950" : "text-slate-800"
          )}
        >
          {text}
        </p>
      </div>

      {/* Radio Circle */}
      <div
        className={cn(
          "h-4 w-4 rounded-full border flex items-center justify-center shrink-0 mt-1 transition-colors",
          isSelected ? "border-indigo-600 bg-indigo-600" : "border-slate-300 bg-white"
        )}
      >
        {isSelected && <span className="h-1.5 w-1.5 rounded-full bg-white block"></span>}
      </div>
    </div>
  );
}
