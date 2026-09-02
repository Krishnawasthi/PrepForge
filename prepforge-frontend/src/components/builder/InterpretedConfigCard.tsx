"use client";

import React from "react";
import { PromptInterpretation } from "@/types/test";
import { Sparkles, Edit3, ArrowRight, CheckCircle2, Shield } from "lucide-react";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";

interface InterpretedConfigCardProps {
  interpretation: PromptInterpretation;
  onEdit: () => void;
  onConfirm: () => void;
  isLoading: boolean;
}

export function InterpretedConfigCard({
  interpretation,
  onEdit,
  onConfirm,
  isLoading,
}: InterpretedConfigCardProps) {
  return (
    <div className="bg-white border-2 border-indigo-200 rounded-2xl p-6 shadow-md space-y-5 animate-in fade-in zoom-in-95 duration-200">
      <div className="flex items-start justify-between gap-4 pb-4 border-b border-slate-100">
        <div>
          <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-indigo-50 text-indigo-700 text-xs font-semibold mb-1">
            <Sparkles className="h-3.5 w-3.5 text-indigo-600" />
            <span>AI Interpretation Successful</span>
          </div>
          <h3 className="text-lg font-bold text-slate-900 tracking-tight">
            {interpretation.goal || "Custom Technical Assessment"}
          </h3>
          <p className="text-xs text-slate-500 italic mt-0.5 line-clamp-2">
            &quot;{interpretation.originalPrompt}&quot;
          </p>
        </div>

        <button
          onClick={onEdit}
          type="button"
          className="inline-flex items-center gap-1 text-xs text-slate-500 hover:text-indigo-600 p-1.5 rounded-lg border border-slate-200 hover:border-indigo-200 transition-colors"
          title="Edit Configuration"
        >
          <Edit3 className="h-3.5 w-3.5" />
          <span className="hidden sm:inline">Modify</span>
        </button>
      </div>

      {/* Interpreted parameter breakdown */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
        <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
          <span className="text-slate-400 font-medium block text-[11px]">Experience</span>
          <span className="font-bold text-slate-800 text-sm mt-0.5 block">{interpretation.experienceLevel}</span>
        </div>

        <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
          <span className="text-slate-400 font-medium block text-[11px]">Difficulty</span>
          <span className="font-bold text-slate-800 text-sm mt-0.5 block">{interpretation.difficulty}</span>
        </div>

        <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
          <span className="text-slate-400 font-medium block text-[11px]">Questions</span>
          <span className="font-bold text-slate-800 text-sm mt-0.5 block">{interpretation.questionCount} Questions</span>
        </div>

        <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
          <span className="text-slate-400 font-medium block text-[11px]">Time Limit</span>
          <span className="font-bold text-slate-800 text-sm mt-0.5 block">{interpretation.timeLimitMinutes} Mins</span>
        </div>
      </div>

      {/* Topics */}
      <div>
        <span className="text-xs font-semibold text-slate-700 block mb-1.5">Target Topics:</span>
        <div className="flex flex-wrap gap-1.5">
          {interpretation.topics.map((topic, i) => (
            <Badge key={i} variant="purple" size="md">
              {topic}
            </Badge>
          ))}
        </div>
      </div>

      {/* Question Types */}
      <div>
        <span className="text-xs font-semibold text-slate-700 block mb-1.5">Selected Formats:</span>
        <div className="flex flex-wrap gap-1.5">
          {interpretation.questionTypes.map((type, i) => (
            <Badge key={i} variant="neutral" size="sm">
              {type}
            </Badge>
          ))}
        </div>
      </div>

      {/* Action buttons */}
      <div className="pt-4 border-t border-slate-100 flex flex-col sm:flex-row items-center justify-between gap-3">
        <span className="text-[11px] text-slate-500 flex items-center gap-1">
          <Shield className="h-3.5 w-3.5 text-emerald-600" />
          Zero login required • Anonymous session
        </span>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <Button
            variant="outline"
            size="md"
            onClick={onEdit}
            className="flex-1 sm:flex-none"
          >
            Customize Parameters
          </Button>

          <Button
            variant="primary"
            size="md"
            onClick={onConfirm}
            isLoading={isLoading}
            className="flex-1 sm:flex-none"
          >
            <span>Proceed to Test</span>
            <ArrowRight className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}
