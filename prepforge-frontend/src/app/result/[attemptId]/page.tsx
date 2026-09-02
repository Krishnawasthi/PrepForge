"use client";

import React, { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { practiceApi } from "@/lib/api-client";
import { PracticeResult } from "@/types/practice";
import { 
  CheckCircle2, 
  XCircle, 
  MinusCircle, 
  RotateCw, 
  ArrowRight, 
  Home, 
  AlertTriangle,
  Lightbulb,
  BookOpen,
  Check,
  X
} from "lucide-react";

export default function PracticeResultPage() {
  const params = useParams();
  const router = useRouter();
  const attemptId = params.attemptId as string;

  const [result, setResult] = useState<PracticeResult | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [startingNewPractice, setStartingNewPractice] = useState<boolean>(false);

  useEffect(() => {
    async function load() {
      if (!attemptId) return;
      try {
        setLoading(true);
        const res = await practiceApi.getAttempt(attemptId);
        if (res.success && res.data) {
          setResult(res.data);
        } else {
          setError(res.message || "Failed to load result.");
        }
      } catch (err: unknown) {
        setError(err instanceof Error ? err.message : "Error connecting to server.");
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [attemptId]);

  const handlePracticeAgain = async () => {
    if (!result) return;
    setStartingNewPractice(true);
    try {
      // Gather all tested topics
      const topics = Array.from(new Set(result.questions.map((q) => q.topic).filter(Boolean)));
      const res = await practiceApi.createTest({
        topics: topics.length > 0 ? topics : ["Core Java"],
        experienceLevel: "Intermediate",
        questionCount: result.totalQuestions || 10,
      });
      if (res.success && res.data?.testId) {
        router.push(`/test/${res.data.testId}`);
      } else {
        router.push("/");
      }
    } catch {
      router.push("/");
    }
  };

  const handlePracticeWeakTopics = async () => {
    if (!result) return;
    setStartingNewPractice(true);
    try {
      const weakTopics = result.weakTopics && result.weakTopics.length > 0
        ? result.weakTopics
        : ["Core Java"];

      const res = await practiceApi.createTest({
        topics: weakTopics,
        experienceLevel: "Intermediate",
        questionCount: Math.min(15, Math.max(5, weakTopics.length * 3)),
      });
      if (res.success && res.data?.testId) {
        router.push(`/test/${res.data.testId}`);
      } else {
        router.push("/");
      }
    } catch {
      router.push("/");
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
        <div className="text-center space-y-3">
          <RotateCw className="h-8 w-8 text-indigo-600 animate-spin mx-auto" />
          <p className="text-sm font-semibold text-slate-700">Scoring practice assessment...</p>
        </div>
      </div>
    );
  }

  if (error || !result) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
        <div className="bg-white border border-slate-200 rounded-2xl p-6 max-w-md w-full text-center space-y-4 shadow-sm">
          <h2 className="text-base font-bold text-slate-900">Result Not Found</h2>
          <p className="text-xs text-slate-600">{error || "Unable to display attempt."}</p>
          <button
            type="button"
            onClick={() => router.push("/")}
            className="w-full py-2.5 bg-indigo-600 text-white rounded-xl text-xs font-bold hover:bg-indigo-700"
          >
            Back to Home
          </button>
        </div>
      </div>
    );
  }

  const mistakeEntries = result.topicMistakes
    ? Object.entries(result.topicMistakes).sort((a, b) => b[1] - a[1])
    : [];

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col justify-between">
      {/* Top Header */}
      <header className="border-b border-slate-200 bg-white sticky top-0 z-20 shadow-2xs">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 py-3.5 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-sm font-black text-slate-900">PrepForge</span>
            <span className="text-xs text-slate-400">/</span>
            <span className="text-xs font-semibold text-indigo-600">Practice Results</span>
          </div>

          <button
            type="button"
            onClick={() => router.push("/")}
            className="text-xs font-bold text-slate-600 hover:text-slate-900 inline-flex items-center gap-1 px-3 py-1.5 rounded-lg border border-slate-200 hover:bg-slate-50 transition-colors"
          >
            <Home className="h-3.5 w-3.5" />
            <span>New Practice</span>
          </button>
        </div>
      </header>

      {/* Main Results Container */}
      <main className="max-w-3xl w-full mx-auto px-4 sm:px-6 py-8 space-y-8">
        {/* 1. Score Summary Hero Card */}
        <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 shadow-xs text-center space-y-6">
          <div className="space-y-1">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">
              Practice Assessment
            </span>
            <h1 className="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">
              Your Result
            </h1>
          </div>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-6 py-2">
            <div className="space-y-0.5">
              <div className="text-5xl sm:text-6xl font-black text-indigo-600 tracking-tight">
                {result.score} <span className="text-3xl font-normal text-slate-400">/ {result.totalQuestions}</span>
              </div>
              <div className="text-sm font-bold text-slate-500">
                {Math.round(result.percentage)}% Overall Accuracy
              </div>
            </div>

            <div className="h-16 w-px bg-slate-200 hidden sm:block" />

            <div className="grid grid-cols-3 gap-3 text-left">
              <div className="p-3 rounded-xl bg-emerald-50 border border-emerald-100 text-center">
                <div className="text-emerald-700 text-base font-black flex items-center justify-center gap-1">
                  <CheckCircle2 className="h-4 w-4" />
                  <span>{result.correctCount}</span>
                </div>
                <span className="text-[11px] font-bold text-emerald-800">Correct</span>
              </div>

              <div className="p-3 rounded-xl bg-rose-50 border border-rose-100 text-center">
                <div className="text-rose-700 text-base font-black flex items-center justify-center gap-1">
                  <XCircle className="h-4 w-4" />
                  <span>{result.incorrectCount}</span>
                </div>
                <span className="text-[11px] font-bold text-rose-800">Incorrect</span>
              </div>

              <div className="p-3 rounded-xl bg-slate-100 border border-slate-200 text-center">
                <div className="text-slate-600 text-base font-black flex items-center justify-center gap-1">
                  <MinusCircle className="h-4 w-4" />
                  <span>{result.skippedCount}</span>
                </div>
                <span className="text-[11px] font-bold text-slate-700">Skipped</span>
              </div>
            </div>
          </div>
        </div>

        {/* 2. Help User Understand What to Fix ("Areas to Improve" & "What You Should Revise") */}
        {mistakeEntries.length > 0 && (
          <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 shadow-xs space-y-6">
            <div className="border-b border-slate-100 pb-3">
              <h2 className="text-base font-bold text-slate-900 flex items-center gap-2">
                <AlertTriangle className="h-4 w-4 text-amber-600" />
                <span>Areas to Improve</span>
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Concepts where mistakes or skipped answers occurred during this session
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
              {mistakeEntries.map(([topic, mistakes]) => (
                <div
                  key={topic}
                  className="flex items-center justify-between p-3 rounded-xl bg-amber-50/60 border border-amber-200 text-xs"
                >
                  <span className="font-bold text-amber-950">{topic}</span>
                  <span className="font-mono font-bold text-amber-800 bg-amber-100/80 px-2 py-0.5 rounded-md">
                    {mistakes} mistake{mistakes > 1 ? "s" : ""}
                  </span>
                </div>
              ))}
            </div>

            {/* Revision Advice */}
            {result.revisionTips && result.revisionTips.length > 0 && (
              <div className="pt-4 border-t border-slate-100 space-y-2.5">
                <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500 flex items-center gap-1.5">
                  <Lightbulb className="h-3.5 w-3.5 text-amber-500" />
                  <span>What You Should Revise</span>
                </h3>
                <ul className="space-y-2">
                  {result.revisionTips.map((tip, idx) => (
                    <li
                      key={idx}
                      className="text-xs text-slate-700 bg-slate-50 border border-slate-200/80 p-3 rounded-xl leading-relaxed flex items-start gap-2"
                    >
                      <span className="text-indigo-600 font-bold shrink-0">•</span>
                      <span>{tip}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}

        {/* 3. Practice Action Buttons (Practice Weak Topics & Practice Again) */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          {mistakeEntries.length > 0 && (
            <button
              type="button"
              disabled={startingNewPractice}
              onClick={handlePracticeWeakTopics}
              className="py-3.5 px-5 rounded-xl bg-amber-600 hover:bg-amber-700 text-white font-bold text-xs shadow-xs transition-colors flex items-center justify-center gap-2 disabled:opacity-60"
            >
              <BookOpen className="h-4 w-4" />
              <span>Practice Weak Topics ({mistakeEntries.length})</span>
            </button>
          )}

          <button
            type="button"
            disabled={startingNewPractice}
            onClick={handlePracticeAgain}
            className="py-3.5 px-5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs shadow-xs transition-colors flex items-center justify-center gap-2 disabled:opacity-60"
          >
            <RotateCw className={`h-4 w-4 ${startingNewPractice ? "animate-spin" : ""}`} />
            <span>Practice Again (New Questions)</span>
            <ArrowRight className="h-4 w-4" />
          </button>
        </div>

        {/* 4. Detailed Question Review List */}
        <div className="space-y-4 pt-4">
          <div className="flex items-center justify-between pb-1">
            <h2 className="text-base font-bold text-slate-900">
              Detailed Question Review ({result.questions.length})
            </h2>
            <span className="text-xs text-slate-500 font-medium">
              Review correct answers & explanations
            </span>
          </div>

          <div className="space-y-4">
            {result.questions.map((q, idx) => {
              const isCorrect = q.isCorrect === true;
              const isSkipped = q.isSkipped === true;

              return (
                <div
                  key={q.id || idx}
                  className={`bg-white border rounded-2xl p-6 shadow-xs space-y-4 transition-all ${
                    isCorrect
                      ? "border-emerald-200 ring-1 ring-emerald-100"
                      : isSkipped
                      ? "border-slate-200"
                      : "border-rose-200 ring-1 ring-rose-100"
                  }`}
                >
                  {/* Question Header */}
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-xs font-bold text-slate-600 bg-slate-100 px-2.5 py-0.5 rounded-md">
                      Q{idx + 1} • {q.topic}
                    </span>

                    {isCorrect ? (
                      <span className="inline-flex items-center gap-1 text-xs font-bold text-emerald-700 bg-emerald-50 px-2.5 py-0.5 rounded-full border border-emerald-200">
                        <Check className="h-3 w-3 stroke-[3]" />
                        <span>Correct</span>
                      </span>
                    ) : isSkipped ? (
                      <span className="inline-flex items-center gap-1 text-xs font-bold text-slate-600 bg-slate-100 px-2.5 py-0.5 rounded-full border border-slate-200">
                        <MinusCircle className="h-3 w-3" />
                        <span>Skipped</span>
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 text-xs font-bold text-rose-700 bg-rose-50 px-2.5 py-0.5 rounded-full border border-rose-200">
                        <X className="h-3 w-3 stroke-[3]" />
                        <span>Incorrect</span>
                      </span>
                    )}
                  </div>

                  {/* Question Statement */}
                  <div className="text-xs sm:text-sm font-semibold text-slate-900 whitespace-pre-line leading-relaxed">
                    {q.question}
                  </div>

                  {/* Options with Status */}
                  <div className="space-y-2 pt-1">
                    {q.options.map((opt, optIdx) => {
                      const isUserChoice = q.userAnswer === opt;
                      const isCorrectChoice = q.correctAnswer === opt;

                      let style = "bg-white border-slate-200 text-slate-700";
                      if (isCorrectChoice) {
                        style = "bg-emerald-50/80 border-emerald-400 text-emerald-950 font-semibold ring-1 ring-emerald-400";
                      } else if (isUserChoice && !isCorrect) {
                        style = "bg-rose-50/80 border-rose-400 text-rose-950 font-semibold ring-1 ring-rose-400";
                      }

                      return (
                        <div
                          key={optIdx}
                          className={`p-3 rounded-xl border text-xs leading-relaxed flex items-start justify-between gap-2 ${style}`}
                        >
                          <span className="flex-1">{opt}</span>
                          {isCorrectChoice && (
                            <span className="text-[11px] font-bold text-emerald-700 shrink-0 uppercase tracking-wider">
                              ✓ Correct Answer
                            </span>
                          )}
                          {isUserChoice && !isCorrect && (
                            <span className="text-[11px] font-bold text-rose-700 shrink-0 uppercase tracking-wider">
                              ✕ Your Choice
                            </span>
                          )}
                        </div>
                      );
                    })}
                  </div>

                  {/* Explanation Box */}
                  {q.explanation && (
                    <div className="mt-3 p-3.5 rounded-xl bg-slate-50 border border-slate-200 text-xs space-y-1">
                      <span className="font-bold text-slate-900 block">Explanation:</span>
                      <p className="text-slate-600 leading-relaxed">{q.explanation}</p>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="py-6 text-center text-xs text-slate-400 border-t border-slate-200/80 bg-white">
        PrepForge — Java Interview Practice Engine
      </footer>
    </div>
  );
}
