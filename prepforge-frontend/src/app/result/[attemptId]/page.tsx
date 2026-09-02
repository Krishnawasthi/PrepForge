"use client";

import React, { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { prepforgeApi } from "@/lib/api-client";
import { TestResult } from "@/types/test";
import { ResultScoreCard } from "@/components/result/ResultScoreCard";
import { PerformanceBreakdown } from "@/components/result/PerformanceBreakdown";
import { WeakStrongAreas } from "@/components/result/WeakStrongAreas";
import { QuestionReviewCard } from "@/components/result/QuestionReviewCard";
import { LoadingExperience } from "@/components/builder/LoadingExperience";
import { Button } from "@/components/ui/Button";
import { RotateCcw, LayoutDashboard, AlertCircle, Sparkles, Download, Printer } from "lucide-react";
import Link from "next/link";

export default function ResultPage() {
  const params = useParams();
  const router = useRouter();
  const attemptId = params.attemptId as string;

  const [result, setResult] = useState<TestResult | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadResult() {
      if (!attemptId) return;
      try {
        setLoading(true);
        const res = await prepforgeApi.getAttemptResult(attemptId);
        if (res.success) {
          setResult(res.data);
        } else {
          setError(res.message || "Unable to retrieve assessment result.");
        }
      } catch (err: unknown) {
        setError(err instanceof Error ? err.message : "Error connecting to results engine.");
      } finally {
        setLoading(false);
      }
    }
    loadResult();
  }, [attemptId]);

  const handleExportJson = () => {
    if (!result) return;
    const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(result, null, 2));
    const downloadAnchor = document.createElement("a");
    downloadAnchor.setAttribute("href", dataStr);
    downloadAnchor.setAttribute("download", `prepforge_result_${result.attemptId}.json`);
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  };

  const handlePrint = () => {
    window.print();
  };

  if (loading) {
    return (
      <div className="py-20">
        <LoadingExperience title="Calculating Assessment Scores & Deep Explanations" />
      </div>
    );
  }

  if (error || !result) {
    return (
      <div className="max-w-md mx-auto my-20 p-8 bg-white rounded-2xl border border-slate-200 shadow-md text-center space-y-4">
        <div className="h-12 w-12 rounded-full bg-rose-50 text-rose-600 flex items-center justify-center mx-auto">
          <AlertCircle className="h-6 w-6" />
        </div>
        <h3 className="text-lg font-bold text-slate-900">Result Error</h3>
        <p className="text-xs text-slate-600">{error || "Result could not be loaded."}</p>
        <Button variant="primary" size="sm" onClick={() => router.push("/create")}>
          Create New Assessment
        </Button>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50/60 py-10 print:bg-white print:py-0">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 space-y-8">
        {/* Top Actions Bar */}
        <div className="flex flex-wrap items-center justify-between gap-3 print:hidden">
          <div className="flex items-center gap-2">
            <Link
              href="/dashboard"
              className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-600 hover:text-slate-900 bg-white border border-slate-200 px-3 py-1.5 rounded-lg shadow-2xs hover:bg-slate-50 transition-colors"
            >
              <LayoutDashboard className="h-3.5 w-3.5 text-indigo-600" />
              <span>Practice Dashboard</span>
            </Link>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleExportJson}
              type="button"
              className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-600 hover:text-slate-900 bg-white border border-slate-200 px-3 py-1.5 rounded-lg shadow-2xs hover:bg-slate-50 transition-colors"
              title="Download JSON summary"
            >
              <Download className="h-3.5 w-3.5 text-indigo-600" />
              <span>Export JSON</span>
            </button>

            <button
              onClick={handlePrint}
              type="button"
              className="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-600 hover:text-slate-900 bg-white border border-slate-200 px-3 py-1.5 rounded-lg shadow-2xs hover:bg-slate-50 transition-colors"
              title="Print / Save PDF"
            >
              <Printer className="h-3.5 w-3.5 text-slate-600" />
              <span>Print / PDF</span>
            </button>

            <Button
              variant="primary"
              size="sm"
              onClick={() => router.push("/create")}
              className="text-xs"
            >
              <RotateCcw className="h-3.5 w-3.5" />
              <span>Create New Test</span>
            </Button>
          </div>
        </div>

        {/* 1. Score Hero Card */}
        <ResultScoreCard
          score={result.score}
          totalQuestions={result.totalQuestions}
          percentage={result.percentage}
          correctCount={result.correctCount}
          incorrectCount={result.incorrectCount}
          skippedCount={result.skippedCount}
          timeTakenSeconds={result.timeTakenSeconds}
          feedbackMessage={result.feedbackMessage}
          testTitle={result.testTitle}
        />

        {/* 2. Performance Breakdown by Topic & Difficulty */}
        <PerformanceBreakdown
          topicAccuracy={result.topicAccuracy}
          difficultyAccuracy={result.difficultyAccuracy}
        />

        {/* 3. Weak & Strong Areas */}
        <WeakStrongAreas
          weakAreas={result.weakAreas || []}
          strongAreas={result.strongAreas || []}
          topicAccuracy={result.topicAccuracy}
        />

        {/* 4. Detailed Question Review List */}
        <div className="space-y-4 pt-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold text-slate-900 tracking-tight flex items-center gap-2">
              <Sparkles className="h-4 w-4 text-indigo-600" />
              <span>Comprehensive Question Solutions</span>
            </h2>
            <span className="text-xs text-slate-400 font-mono">
              {result.questions.length} questions analyzed
            </span>
          </div>

          <div className="space-y-4">
            {result.questions.map((q, idx) => (
              <QuestionReviewCard key={q.questionId || idx} question={q} index={idx} />
            ))}
          </div>
        </div>

        {/* Bottom Call to Action */}
        <div className="text-center pt-8 pb-4 print:hidden">
          <Button
            variant="primary"
            size="lg"
            onClick={() => router.push("/create")}
            className="shadow-md"
          >
            <RotateCcw className="h-4 w-4" />
            <span>Start Your Next Assessment</span>
          </Button>
        </div>
      </div>
    </div>
  );
}
