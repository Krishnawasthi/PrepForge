"use client";

import React, { useEffect, useState, useRef, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";
import { practiceApi } from "@/lib/api-client";
import { PracticeTest, Question } from "@/types/practice";
import { 
  ChevronLeft, 
  ChevronRight, 
  RefreshCw, 
  CheckCircle, 
  Clock, 
  AlertCircle,
  RotateCcw,
  Check
} from "lucide-react";

export default function PracticeTestPage() {
  const params = useParams();
  const router = useRouter();
  const testId = params.testId as string;

  const [test, setTest] = useState<PracticeTest | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [isChangingQuestion, setIsChangingQuestion] = useState<boolean>(false);
  const [changeError, setChangeError] = useState<string | null>(null);
  const [showSubmitModal, setShowSubmitModal] = useState<boolean>(false);

  // Time tracking
  const startTimeRef = useRef<number>(Date.now());
  const [secondsRemaining, setSecondsRemaining] = useState<number>(1200);
  const isSubmittedRef = useRef<boolean>(false);
  const usedQuestionsRef = useRef<Set<string>>(new Set());

  // Load test
  useEffect(() => {
    async function load() {
      if (!testId) return;
      try {
        setLoading(true);
        const res = await practiceApi.getTest(testId);
        if (res.success && res.data) {
          setTest(res.data);
          startTimeRef.current = Date.now();
          setSecondsRemaining((res.data.timeLimitMinutes || 20) * 60);
          res.data.questions.forEach((q) => {
            if (q.question) usedQuestionsRef.current.add(q.question.trim());
          });
        } else {
          setError(res.message || "Unable to load practice questions.");
        }
      } catch (err: unknown) {
        setError(err instanceof Error ? err.message : "Error connecting to practice server.");
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [testId]);

  // Countdown timer
  useEffect(() => {
    if (loading || isSubmittedRef.current || secondsRemaining <= 0) return;
    const interval = setInterval(() => {
      setSecondsRemaining((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          handleSubmitTest();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [loading, secondsRemaining]);

  const handleSelectAnswer = (option: string) => {
    if (!test) return;
    const currentQ = test.questions[currentIndex];
    if (!currentQ) return;
    setAnswers((prev) => ({
      ...prev,
      [currentQ.id]: option,
    }));
  };

  const handleClearAnswer = () => {
    if (!test) return;
    const currentQ = test.questions[currentIndex];
    if (!currentQ) return;
    setAnswers((prev) => {
      const next = { ...prev };
      delete next[currentQ.id];
      return next;
    });
  };

  const handleChangeQuestion = async () => {
    if (!test || isChangingQuestion) return;
    const currentQ = test.questions[currentIndex];
    if (!currentQ) return;

    setChangeError(null);
    setIsChangingQuestion(true);

    try {
      const previouslyUsed = Array.from(usedQuestionsRef.current);
      const res = await practiceApi.changeQuestion(testId, currentQ.id, {
        topic: currentQ.topic,
        difficulty: currentQ.difficulty,
        experienceLevel: test.experienceLevel,
        previouslyUsedQuestions: previouslyUsed,
      });

      if (res.success && res.data) {
        const replacement = res.data;
        if (replacement.question) {
          usedQuestionsRef.current.add(replacement.question.trim());
        }

        // Update in-place at currentIndex without changing question count
        setTest((prev) => {
          if (!prev) return prev;
          const updated = [...prev.questions];
          updated[currentIndex] = replacement;
          return { ...prev, questions: updated };
        });

        // Reset the answer for this question slot
        setAnswers((prev) => {
          const next = { ...prev };
          delete next[currentQ.id];
          return next;
        });
      } else {
        setChangeError(res.message || "Could not generate a new question right now. Please try again.");
      }
    } catch (err: unknown) {
      setChangeError(err instanceof Error ? err.message : "Could not change question. Please try again.");
    } finally {
      setIsChangingQuestion(false);
    }
  };

  const handleSubmitTest = useCallback(async () => {
    if (isSubmittedRef.current || isSubmitting || !test) return;
    isSubmittedRef.current = true;
    setIsSubmitting(true);
    setShowSubmitModal(false);

    const timeTakenSeconds = Math.max(1, Math.round((Date.now() - startTimeRef.current) / 1000));

    try {
      const res = await practiceApi.submitTest(testId, {
        answers,
        timeTakenSeconds,
      });

      if (res.success && res.data?.attemptId) {
        router.push(`/result/${res.data.attemptId}`);
      } else {
        setError(res.message || "Failed to submit test.");
        setIsSubmitting(false);
        isSubmittedRef.current = false;
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Error submitting practice test.");
      setIsSubmitting(false);
      isSubmittedRef.current = false;
    }
  }, [test, isSubmitting, testId, answers, router]);

  const formatTimer = (totalSeconds: number) => {
    const mins = Math.floor(totalSeconds / 60);
    const secs = totalSeconds % 60;
    return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
        <div className="text-center space-y-3">
          <RefreshCw className="h-8 w-8 text-indigo-600 animate-spin mx-auto" />
          <p className="text-sm font-semibold text-slate-700">Loading your practice test...</p>
        </div>
      </div>
    );
  }

  if (error || !test) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
        <div className="bg-white border border-slate-200 rounded-2xl p-6 max-w-md w-full text-center space-y-4 shadow-sm">
          <AlertCircle className="h-10 w-10 text-rose-600 mx-auto" />
          <h2 className="text-base font-bold text-slate-900">Unable to Open Practice</h2>
          <p className="text-xs text-slate-600">{error || "Test not found."}</p>
          <button
            type="button"
            onClick={() => router.push("/")}
            className="w-full py-2.5 bg-indigo-600 text-white rounded-xl text-xs font-bold hover:bg-indigo-700 transition-colors"
          >
            Back to Practice Setup
          </button>
        </div>
      </div>
    );
  }

  const currentQ = test.questions[currentIndex];
  const total = test.questions.length;
  const answeredCount = Object.keys(answers).filter((k) => answers[k] && answers[k].trim()).length;
  const isLastQuestion = currentIndex === total - 1;
  const optionLetters = ["A", "B", "C", "D"];

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col justify-between">
      {/* Top Test Header with Timer & Progress */}
      <header className="border-b border-slate-200 bg-white sticky top-0 z-20 shadow-2xs">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 py-3.5 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="text-xs font-bold text-indigo-700 bg-indigo-50 border border-indigo-100 px-2.5 py-1 rounded-lg">
              Question {currentIndex + 1} of {total}
            </span>
            <span className="text-xs text-slate-500 font-medium hidden sm:inline">
              ({answeredCount} of {total} answered)
            </span>
          </div>

          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1.5 font-mono text-xs font-bold text-slate-700 bg-slate-100 border border-slate-200 px-2.5 py-1 rounded-lg">
              <Clock className="h-3.5 w-3.5 text-indigo-600" />
              <span>{formatTimer(secondsRemaining)}</span>
            </div>

            <button
              type="button"
              onClick={() => setShowSubmitModal(true)}
              className="text-xs font-bold px-3.5 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white shadow-2xs transition-colors"
            >
              Submit Test
            </button>
          </div>
        </div>
      </header>

      {/* Main Question Card Container */}
      <main className="max-w-3xl w-full mx-auto px-4 sm:px-6 py-8">
        {changeError && (
          <div className="mb-4 p-3 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-xs flex items-center justify-between">
            <span>{changeError}</span>
            <button
              type="button"
              onClick={() => setChangeError(null)}
              className="font-bold text-amber-700 hover:text-amber-900 ml-2"
            >
              Dismiss
            </button>
          </div>
        )}

        {currentQ && (
          <div className="bg-white border border-slate-200 rounded-2xl p-6 sm:p-8 shadow-xs space-y-6">
            {/* Question Meta */}
            <div className="flex items-center justify-between gap-2 border-b border-slate-100 pb-3">
              <span className="text-xs font-bold text-slate-600 bg-slate-100 px-2.5 py-0.5 rounded-md">
                {currentQ.topic}
              </span>
              {currentQ.difficulty && (
                <span className="text-[11px] font-semibold text-slate-500">
                  {currentQ.difficulty}
                </span>
              )}
            </div>

            {/* Question Text */}
            <div className="text-sm sm:text-base font-semibold text-slate-900 leading-relaxed whitespace-pre-line font-sans">
              {currentQ.question}
            </div>

            {/* 4 Options */}
            <div className="space-y-2.5 pt-2">
              {currentQ.options.map((option, idx) => {
                const letter = optionLetters[idx] || `${idx + 1}`;
                const isSelected = answers[currentQ.id] === option;
                return (
                  <button
                    type="button"
                    key={idx}
                    onClick={() => handleSelectAnswer(option)}
                    className={`w-full p-4 rounded-xl border text-left text-xs sm:text-sm font-medium transition-all flex items-start gap-3 ${
                      isSelected
                        ? "bg-indigo-50/80 border-indigo-600 text-indigo-950 ring-1 ring-indigo-600 font-semibold shadow-xs"
                        : "bg-white border-slate-200 text-slate-700 hover:border-slate-300 hover:bg-slate-50/60"
                    }`}
                  >
                    <div
                      className={`h-6 w-6 rounded-lg shrink-0 flex items-center justify-center text-xs font-bold border transition-colors ${
                        isSelected
                          ? "bg-indigo-600 border-indigo-600 text-white"
                          : "bg-slate-100 border-slate-200 text-slate-600"
                      }`}
                    >
                      {letter}
                    </div>
                    <span className="mt-0.5 leading-relaxed flex-1">{option}</span>
                  </button>
                );
              })}
            </div>

            {/* Bottom Controls */}
            <div className="pt-6 border-t border-slate-100 flex flex-col sm:flex-row items-center justify-between gap-3">
              <div>
                {answers[currentQ.id] && (
                  <button
                    type="button"
                    onClick={handleClearAnswer}
                    className="inline-flex items-center gap-1 text-xs text-slate-500 hover:text-rose-600 font-medium transition-colors"
                  >
                    <RotateCcw className="h-3 w-3" />
                    <span>Clear Answer</span>
                  </button>
                )}
              </div>

              <div className="flex items-center gap-2.5 w-full sm:w-auto">
                <button
                  type="button"
                  onClick={() => setCurrentIndex((prev) => Math.max(0, prev - 1))}
                  disabled={currentIndex === 0 || isChangingQuestion}
                  className="flex-1 sm:flex-none px-3.5 py-2 rounded-xl border border-slate-200 bg-white text-slate-700 text-xs font-bold hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed inline-flex items-center justify-center gap-1"
                >
                  <ChevronLeft className="h-4 w-4" />
                  <span>Previous</span>
                </button>

                <button
                  type="button"
                  onClick={handleChangeQuestion}
                  disabled={isChangingQuestion}
                  title="Replace this question with another question testing the same topic"
                  className="flex-1 sm:flex-none px-3.5 py-2 rounded-xl border border-amber-200 bg-amber-50/60 text-amber-900 text-xs font-bold hover:bg-amber-100/70 disabled:opacity-60 inline-flex items-center justify-center gap-1.5 transition-colors"
                >
                  <RefreshCw className={`h-3.5 w-3.5 text-amber-600 ${isChangingQuestion ? "animate-spin" : ""}`} />
                  <span>{isChangingQuestion ? "Creating..." : "Change Question"}</span>
                </button>

                {isLastQuestion ? (
                  <button
                    type="button"
                    onClick={() => setShowSubmitModal(true)}
                    disabled={isChangingQuestion}
                    className="flex-1 sm:flex-none px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold inline-flex items-center justify-center gap-1 shadow-xs transition-colors"
                  >
                    <span>Submit Test</span>
                    <CheckCircle className="h-4 w-4" />
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={() => setCurrentIndex((prev) => Math.min(total - 1, prev + 1))}
                    disabled={isChangingQuestion}
                    className="flex-1 sm:flex-none px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold inline-flex items-center justify-center gap-1 shadow-xs transition-colors"
                  >
                    <span>Next</span>
                    <ChevronRight className="h-4 w-4" />
                  </button>
                )}
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Confirmation Modal */}
      {showSubmitModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white border border-slate-200 rounded-2xl p-6 max-w-sm w-full space-y-4 shadow-xl animate-in fade-in">
            <h3 className="text-base font-bold text-slate-900">Submit Practice Test?</h3>
            <p className="text-xs text-slate-600 leading-relaxed">
              You answered <strong className="text-slate-900">{answeredCount}</strong> of <strong className="text-slate-900">{total}</strong> questions.
              {total - answeredCount > 0 && (
                <span className="block mt-1 text-amber-700 font-medium">
                  {total - answeredCount} questions will be scored as skipped.
                </span>
              )}
            </p>
            <div className="flex items-center gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowSubmitModal(false)}
                className="flex-1 py-2 rounded-xl border border-slate-200 text-xs font-bold text-slate-700 hover:bg-slate-50"
              >
                Keep Reviewing
              </button>
              <button
                type="button"
                disabled={isSubmitting}
                onClick={handleSubmitTest}
                className="flex-1 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold shadow-xs flex items-center justify-center gap-1"
              >
                {isSubmitting ? (
                  <RefreshCw className="h-3.5 w-3.5 animate-spin" />
                ) : (
                  <span>Submit Now</span>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Footer */}
      <footer className="py-4 text-center text-xs text-slate-400">
        PrepForge Practice Engine
      </footer>
    </div>
  );
}
