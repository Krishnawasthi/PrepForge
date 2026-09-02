"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { getLocalHistory, computeDashboardStats, LocalHistoryItem, clearLocalHistory } from "@/lib/history";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { 
  Trophy, 
  Target, 
  Clock, 
  TrendingUp, 
  ShieldCheck, 
  RotateCcw, 
  ArrowRight, 
  Sparkles, 
  Trash2,
  Calendar,
  ExternalLink,
  Laptop
} from "lucide-react";
import { formatTime } from "@/lib/utils";

export function AnonymousDashboard() {
  const router = useRouter();
  const [history, setHistory] = useState<LocalHistoryItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setHistory(getLocalHistory());
    setLoading(false);
  }, []);

  const stats = computeDashboardStats(history);

  const handleClearHistory = () => {
    if (confirm("Clear local device practice history?")) {
      clearLocalHistory();
      setHistory([]);
    }
  };

  if (loading) {
    return <div className="py-20 text-center text-xs text-slate-400">Loading practice data...</div>;
  }

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 space-y-8 py-8">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-200">
        <div>
          <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-slate-100 text-slate-700 text-xs font-semibold mb-1">
            <Laptop className="h-3.5 w-3.5 text-indigo-600" />
            <span>Device-Based Anonymous History</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">
            Practice & Progress Dashboard
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
            Track test scores, identify growth areas, and resume recent assessments on this browser.
          </p>
        </div>

        <div className="flex items-center gap-2">
          {history.length > 0 && (
            <button
              onClick={handleClearHistory}
              type="button"
              className="inline-flex items-center gap-1 text-xs text-slate-400 hover:text-rose-600 p-2 rounded-lg border border-slate-200 hover:border-rose-200 transition-colors"
              title="Clear Local History"
            >
              <Trash2 className="h-3.5 w-3.5" />
              <span className="hidden sm:inline">Reset History</span>
            </button>
          )}

          <Button variant="primary" size="sm" onClick={() => router.push("/create")}>
            <RotateCcw className="h-3.5 w-3.5" />
            <span>Create New Test</span>
          </Button>
        </div>
      </div>

      {/* Empty state (Requirement #36: "Start your first test to see your progress.") */}
      {history.length === 0 ? (
        <div className="text-center py-16 bg-white border border-slate-200 rounded-2xl p-8 max-w-lg mx-auto space-y-4 shadow-sm">
          <div className="h-12 w-12 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center mx-auto">
            <Target className="h-6 w-6" />
          </div>
          <h3 className="text-lg font-bold text-slate-900">No Assessment History Found</h3>
          <p className="text-xs text-slate-500 leading-relaxed max-w-sm mx-auto">
            Start your first test to see your progress, track topic strengths, and identify growth areas.
          </p>
          <div className="pt-2">
            <Button variant="primary" size="md" onClick={() => router.push("/create")}>
              <span>Start Your First Test</span>
              <ArrowRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      ) : (
        <>
          {/* Cumulative KPI Metrics */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <Card className="p-4 bg-white border-slate-200 space-y-1">
              <span className="text-[11px] font-medium text-slate-400 flex items-center gap-1">
                <Target className="h-3.5 w-3.5 text-indigo-600" />
                Tests Completed
              </span>
              <p className="text-2xl font-black text-slate-900 font-mono">
                {stats.testsCompleted}
              </p>
            </Card>

            <Card className="p-4 bg-white border-slate-200 space-y-1">
              <span className="text-[11px] font-medium text-slate-400 flex items-center gap-1">
                <Sparkles className="h-3.5 w-3.5 text-indigo-600" />
                Questions Solved
              </span>
              <p className="text-2xl font-black text-slate-900 font-mono">
                {stats.questionsSolved}
              </p>
            </Card>

            <Card className="p-4 bg-white border-slate-200 space-y-1">
              <span className="text-[11px] font-medium text-slate-400 flex items-center gap-1">
                <Trophy className="h-3.5 w-3.5 text-amber-500" />
                Best Score
              </span>
              <p className="text-2xl font-black text-emerald-600 font-mono">
                {stats.bestScore}%
              </p>
            </Card>

            <Card className="p-4 bg-white border-slate-200 space-y-1">
              <span className="text-[11px] font-medium text-slate-400 flex items-center gap-1">
                <Clock className="h-3.5 w-3.5 text-indigo-600" />
                Average Score
              </span>
              <p className="text-2xl font-black text-indigo-600 font-mono">
                {stats.averageScore}%
              </p>
            </Card>
          </div>

          {/* Cumulative Weak vs Strong Areas */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Weak Areas */}
            <Card className="p-5 bg-white border-slate-200 space-y-3">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700 flex items-center gap-1.5">
                <TrendingUp className="h-4 w-4 text-amber-600" />
                <span>Aggregated Growth Areas</span>
              </h3>
              {stats.accumulatedWeakAreas.length === 0 ? (
                <p className="text-xs text-slate-400">All recent topics completed with high accuracy.</p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {stats.accumulatedWeakAreas.map((topic) => (
                    <Badge key={topic} variant="warning" size="md">
                      {topic}
                    </Badge>
                  ))}
                </div>
              )}
            </Card>

            {/* Strong Areas */}
            <Card className="p-5 bg-white border-slate-200 space-y-3">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-700 flex items-center gap-1.5">
                <ShieldCheck className="h-4 w-4 text-emerald-600" />
                <span>Demonstrated Strengths</span>
              </h3>
              {stats.accumulatedStrongAreas.length === 0 ? (
                <p className="text-xs text-slate-400">Complete more tests to highlight your strongest tracks.</p>
              ) : (
                <div className="flex flex-wrap gap-2">
                  {stats.accumulatedStrongAreas.map((topic) => (
                    <Badge key={topic} variant="success" size="md">
                      {topic}
                    </Badge>
                  ))}
                </div>
              )}
            </Card>
          </div>

          {/* Recent Completed Tests Table / Cards */}
          <div className="space-y-3">
            <h2 className="text-base font-bold text-slate-900 tracking-tight">
              Recent Assessment Sessions
            </h2>

            <div className="space-y-2.5">
              {history.map((item) => (
                <div
                  key={item.attemptId}
                  className="bg-white border border-slate-200 rounded-xl p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:border-slate-300 hover:shadow-xs transition-all"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-slate-900 text-sm">{item.testTitle}</span>
                      <span className="text-[11px] font-mono text-slate-400">({item.totalQuestions} Qs)</span>
                    </div>
                    <div className="flex items-center gap-3 text-xs text-slate-500">
                      <span className="flex items-center gap-1">
                        <Calendar className="h-3 w-3 text-slate-400" />
                        {new Date(item.completedAt).toLocaleDateString()}
                      </span>
                      <span>•</span>
                      <span className="flex items-center gap-1">
                        <Clock className="h-3 w-3 text-slate-400" />
                        {formatTime(item.timeTakenSeconds)}
                      </span>
                    </div>
                  </div>

                  <div className="flex items-center justify-between sm:justify-end gap-4">
                    <div className="text-right">
                      <span className="text-xs text-slate-400 block">Score</span>
                      <span
                        className={`text-base font-black font-mono ${
                          item.percentage >= 70
                            ? "text-emerald-600"
                            : item.percentage >= 50
                            ? "text-amber-600"
                            : "text-rose-600"
                        }`}
                      >
                        {Math.round(item.percentage)}%
                      </span>
                    </div>

                    <Link
                      href={`/result/${item.attemptId}`}
                      className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg border border-slate-200 bg-slate-50 hover:bg-indigo-50 hover:text-indigo-700 hover:border-indigo-200 text-xs font-semibold text-slate-700 transition-colors"
                    >
                      <span>View Solutions</span>
                      <ExternalLink className="h-3 w-3" />
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
