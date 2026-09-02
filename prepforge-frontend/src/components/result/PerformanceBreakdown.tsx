"use client";

import React from "react";
import { BarChart3, Layers, Gauge } from "lucide-react";

interface PerformanceBreakdownProps {
  topicAccuracy: Record<string, number>;
  difficultyAccuracy: Record<string, number>;
}

export function PerformanceBreakdown({
  topicAccuracy,
  difficultyAccuracy,
}: PerformanceBreakdownProps) {
  const topics = Object.entries(topicAccuracy || {});
  const difficulties = Object.entries(difficultyAccuracy || {});

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {/* Topic Accuracy Breakdown */}
      <div className="bg-white border border-slate-200/90 rounded-2xl p-6 shadow-xs space-y-4">
        <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2 pb-3 border-b border-slate-100">
          <Layers className="h-4 w-4 text-indigo-600" />
          <span>Topic Mastery Breakdown</span>
        </h3>

        {topics.length === 0 ? (
          <p className="text-xs text-slate-400">No topic data available.</p>
        ) : (
          <div className="space-y-3">
            {topics.map(([topic, acc]) => {
              const isGood = acc >= 65;
              return (
                <div key={topic} className="space-y-1">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-semibold text-slate-800">{topic}</span>
                    <span className={`font-mono font-bold ${isGood ? "text-emerald-700" : "text-amber-700"}`}>
                      {Math.round(acc)}%
                    </span>
                  </div>
                  <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${
                        isGood ? "bg-emerald-500" : "bg-amber-500"
                      }`}
                      style={{ width: `${Math.max(4, acc)}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Difficulty Rigor Breakdown */}
      <div className="bg-white border border-slate-200/90 rounded-2xl p-6 shadow-xs space-y-4">
        <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2 pb-3 border-b border-slate-100">
          <Gauge className="h-4 w-4 text-indigo-600" />
          <span>Difficulty Accuracy</span>
        </h3>

        {difficulties.length === 0 ? (
          <p className="text-xs text-slate-400">No difficulty data available.</p>
        ) : (
          <div className="space-y-3">
            {difficulties.map(([diff, acc]) => {
              const isGood = acc >= 65;
              return (
                <div key={diff} className="space-y-1">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-semibold text-slate-800">{diff}</span>
                    <span className={`font-mono font-bold ${isGood ? "text-indigo-700" : "text-slate-600"}`}>
                      {Math.round(acc)}%
                    </span>
                  </div>
                  <div className="w-full bg-slate-100 h-2 rounded-full overflow-hidden">
                    <div
                      className="bg-indigo-600 h-full rounded-full transition-all duration-500"
                      style={{ width: `${Math.max(4, acc)}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
