"use client";

import React, { useState } from "react";
import { TrendingUp, ShieldCheck, Zap, ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { prepforgeApi } from "@/lib/api-client";
import { useRouter } from "next/navigation";

interface WeakStrongAreasProps {
  weakAreas: string[];
  strongAreas: string[];
  topicAccuracy: Record<string, number>;
}

export function WeakStrongAreas({
  weakAreas,
  strongAreas,
  topicAccuracy,
}: WeakStrongAreasProps) {
  const router = useRouter();
  const [generatingPractice, setGeneratingPractice] = useState(false);

  const handlePracticeWeakAreas = async () => {
    if (weakAreas.length === 0) return;
    setGeneratingPractice(true);
    try {
      const res = await prepforgeApi.generateWeakAreaPractice(weakAreas, 10);
      if (res.success) {
        router.push(`/test/${res.data.testId}`);
      }
    } catch (err) {
      console.error("Failed to generate weak area practice test:", err);
      setGeneratingPractice(false);
    }
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {/* Weak Areas (Growth Areas) */}
      <div className="bg-white border border-slate-200/90 rounded-2xl p-6 shadow-xs space-y-4 flex flex-col justify-between">
        <div className="space-y-3">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <TrendingUp className="h-4 w-4 text-amber-600" />
              <span>Your Next Growth Areas</span>
            </h3>
            <span className="text-[11px] text-amber-700 bg-amber-50 px-2 py-0.5 rounded-md font-semibold">
              Focus Topics
            </span>
          </div>

          {weakAreas.length === 0 ? (
            <p className="text-xs text-slate-500 py-4">
              Great job! You showed strong mastery across all tested topics.
            </p>
          ) : (
            <div className="space-y-2">
              {weakAreas.map((topic) => {
                const acc = topicAccuracy[topic] ?? 50;
                return (
                  <div
                    key={topic}
                    className="flex items-center justify-between p-3 rounded-xl bg-amber-50/50 border border-amber-100 text-xs"
                  >
                    <span className="font-semibold text-slate-800">{topic}</span>
                    <span className="font-mono font-bold text-amber-700">{Math.round(acc)}% Accuracy</span>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {weakAreas.length > 0 && (
          <div className="pt-4 border-t border-slate-100">
            <Button
              variant="primary"
              size="sm"
              onClick={handlePracticeWeakAreas}
              isLoading={generatingPractice}
              className="w-full bg-amber-600 hover:bg-amber-700 focus:ring-amber-500 text-xs"
            >
              <Zap className="h-3.5 w-3.5" />
              <span>Practice Weak Areas (10 Qs)</span>
            </Button>
          </div>
        )}
      </div>

      {/* Strong Areas */}
      <div className="bg-white border border-slate-200/90 rounded-2xl p-6 shadow-xs space-y-3">
        <div className="flex items-center justify-between pb-3 border-b border-slate-100">
          <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
            <ShieldCheck className="h-4 w-4 text-emerald-600" />
            <span>You&apos;re Strong In</span>
          </h3>
          <span className="text-[11px] text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-md font-semibold">
            Demonstrated Strengths
          </span>
        </div>

        {strongAreas.length === 0 ? (
          <p className="text-xs text-slate-500 py-4">
            Keep practicing to build your standout strength areas!
          </p>
        ) : (
          <div className="space-y-2">
            {strongAreas.map((topic) => {
              const acc = topicAccuracy[topic] ?? 85;
              return (
                <div
                  key={topic}
                  className="flex items-center justify-between p-3 rounded-xl bg-emerald-50/50 border border-emerald-100 text-xs"
                >
                  <span className="font-semibold text-slate-800">{topic}</span>
                  <span className="font-mono font-bold text-emerald-700">{Math.round(acc)}% Accuracy</span>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
