"use client";

import React, { useEffect, useState, useRef, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";
import { prepforgeApi } from "@/lib/api-client";
import { TestDetail } from "@/types/test";
import { TestHeader } from "@/components/test/TestHeader";
import { QuestionCard } from "@/components/test/QuestionCard";
import { QuestionNavigator } from "@/components/test/QuestionNavigator";
import { SubmissionConfirmModal } from "@/components/test/SubmissionConfirmModal";
import { LoadingExperience } from "@/components/builder/LoadingExperience";
import { saveAttemptToLocalHistory } from "@/lib/history";
import { getAnonymousSessionId } from "@/lib/session";
import { AlertCircle, Keyboard } from "lucide-react";
import { Button } from "@/components/ui/Button";

export default function TakeTestPage() {
  const params = useParams();
  const router = useRouter();
  const testId = params.testId as string;

  const [testDetail, setTestDetail] = useState<TestDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Examination State
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [markedForReview, setMarkedForReview] = useState<Set<number>>(new Set());
  const [showSubmitModal, setShowSubmitModal] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Time tracking
  const startTimeRef = useRef<number>(Date.now());
  const isSubmittedRef = useRef<boolean>(false);

  // Fetch test details on mount
  useEffect(() => {
    async function loadTest() {
      if (!testId) return;
      try {
        setLoading(true);
        const res = await prepforgeApi.getTestDetail(testId);
        if (res.success) {
          setTestDetail(res.data);
          startTimeRef.current = Date.now();
        } else {
          setError(res.message || "Unable to load test questions.");
        }
      } catch (err: unknown) {
        setError(err instanceof Error ? err.message : "Error connecting to test engine.");
      } finally {
        setLoading(false);
      }
    }
    loadTest();
  }, [testId]);

  const handleSelectAnswer = useCallback((answer: string) => {
    if (!testDetail) return;
    const currentQ = testDetail.questions[currentIndex];
    setAnswers((prev) => ({
      ...prev,
      [currentQ.id]: answer,
    }));
  }, [testDetail, currentIndex]);

  const handleClearAnswer = useCallback(() => {
    if (!testDetail) return;
    const currentQ = testDetail.questions[currentIndex];
    setAnswers((prev) => {
      const next = { ...prev };
      delete next[currentQ.id];
      return next;
    });
  }, [testDetail, currentIndex]);

  const handleToggleMarkReview = useCallback(() => {
    setMarkedForReview((prev) => {
      const next = new Set(prev);
      if (next.has(currentIndex)) {
        next.delete(currentIndex);
      } else {
        next.add(currentIndex);
      }
      return next;
    });
  }, [currentIndex]);

  const handleSubmit = useCallback(async () => {
    if (isSubmittedRef.current || isSubmitting || !testDetail) return;

    isSubmittedRef.current = true;
    setIsSubmitting(true);
    setShowSubmitModal(false);

    const timeTakenSeconds = Math.max(1, Math.round((Date.now() - startTimeRef.current) / 1000));
    const attemptId = `att_${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;

    try {
      const res = await prepforgeApi.submitTest(testId, {
        anonymousSessionId: getAnonymousSessionId(),
        attemptId,
        answers,
        timeTakenSeconds,
      });

      if (res.success) {
        // Save to anonymous local history (Requirement #35 & #36)
        saveAttemptToLocalHistory({
          attemptId: res.data.attemptId,
          testId: res.data.testId,
          testTitle: res.data.testTitle,
          score: res.data.score,
          totalQuestions: res.data.totalQuestions,
          percentage: res.data.percentage,
          timeTakenSeconds: res.data.timeTakenSeconds,
          completedAt: res.data.completedAt,
          weakAreas: res.data.weakAreas || [],
          strongAreas: res.data.strongAreas || [],
        });

        // Redirect to result page
        router.push(`/result/${res.data.attemptId}`);
      } else {
        setError(res.message || "Failed to calculate test score.");
        setIsSubmitting(false);
        isSubmittedRef.current = false;
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Error submitting test result.");
      setIsSubmitting(false);
      isSubmittedRef.current = false;
    }
  }, [isSubmitting, testDetail, testId, answers, router]);

  // Keyboard Shortcuts (A, B, C, D, 1, 2, 3, 4, ArrowLeft, ArrowRight, M)
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (!testDetail || showSubmitModal) return;
      // Do not trigger if user is in an input or textarea
      if (["INPUT", "TEXTAREA"].includes((e.target as HTMLElement).tagName)) return;

      const q = testDetail.questions[currentIndex];
      if (!q) return;

      const key = e.key.toUpperCase();

      if (key === "A" || key === "1") {
        if (q.options[0]) handleSelectAnswer(q.options[0]);
      } else if (key === "B" || key === "2") {
        if (q.options[1]) handleSelectAnswer(q.options[1]);
      } else if (key === "C" || key === "3") {
        if (q.options[2]) handleSelectAnswer(q.options[2]);
      } else if (key === "D" || key === "4") {
        if (q.options[3]) handleSelectAnswer(q.options[3]);
      } else if (key === "M") {
        handleToggleMarkReview();
      } else if (key === "ARROWLEFT") {
        setCurrentIndex((prev) => Math.max(0, prev - 1));
      } else if (key === "ARROWRIGHT") {
        setCurrentIndex((prev) => Math.min(testDetail.questions.length - 1, prev + 1));
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [testDetail, currentIndex, showSubmitModal, handleSelectAnswer, handleToggleMarkReview]);

  if (loading) {
    return (
      <div className="py-20">
        <LoadingExperience title="Initializing Assessment Environment" />
      </div>
    );
  }

  if (error || !testDetail) {
    return (
      <div className="max-w-md mx-auto my-20 p-8 bg-white rounded-2xl border border-slate-200 shadow-md text-center space-y-4">
        <div className="h-12 w-12 rounded-full bg-rose-50 text-rose-600 flex items-center justify-center mx-auto">
          <AlertCircle className="h-6 w-6" />
        </div>
        <h3 className="text-lg font-bold text-slate-900">Assessment Error</h3>
        <p className="text-xs text-slate-600">{error || "Test could not be loaded."}</p>
        <Button variant="primary" size="sm" onClick={() => router.push("/create")}>
          Create New Assessment
        </Button>
      </div>
    );
  }

  const currentQuestion = testDetail.questions[currentIndex];
  const questionIds = testDetail.questions.map((q) => q.id);
  const answeredCount = Object.keys(answers).filter((k) => answers[k] && answers[k].trim()).length;
  const unansweredCount = testDetail.questions.length - answeredCount;

  return (
    <div className="min-h-screen bg-slate-50/60 pb-16">
      {/* Top Fixed Header with Timer */}
      <TestHeader
        title={testDetail.title}
        currentIndex={currentIndex}
        totalQuestions={testDetail.questions.length}
        timeLimitMinutes={testDetail.timeLimitMinutes}
        onTimeExpire={handleSubmit}
        onSubmitClick={() => setShowSubmitModal(true)}
        isSubmitting={isSubmitting}
      />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-6">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6 items-start">
          {/* Main Question Card (3 cols) */}
          <div className="lg:col-span-3 space-y-3">
            {currentQuestion && (
              <QuestionCard
                question={currentQuestion}
                index={currentIndex}
                total={testDetail.questions.length}
                selectedAnswer={answers[currentQuestion.id]}
                onSelectAnswer={handleSelectAnswer}
                onClearAnswer={handleClearAnswer}
                isMarkedForReview={markedForReview.has(currentIndex)}
                onToggleMarkReview={handleToggleMarkReview}
                onPrevious={() => setCurrentIndex((prev) => Math.max(0, prev - 1))}
                onNext={() => setCurrentIndex((prev) => Math.min(testDetail.questions.length - 1, prev + 1))}
                hasPrevious={currentIndex > 0}
                hasNext={currentIndex < testDetail.questions.length - 1}
              />
            )}

            {/* Keyboard shortcuts footer hint */}
            <div className="hidden sm:flex items-center justify-between px-3 py-2 bg-white/60 border border-slate-200/70 rounded-xl text-[11px] text-slate-500">
              <span className="flex items-center gap-1.5">
                <Keyboard className="h-3.5 w-3.5 text-indigo-600" />
                <span>Keyboard Shortcuts:</span>
              </span>
              <div className="flex items-center gap-3 font-mono">
                <span><kbd className="px-1.5 py-0.5 bg-slate-100 border border-slate-200 rounded">A</kbd>-<kbd className="px-1.5 py-0.5 bg-slate-100 border border-slate-200 rounded">D</kbd> Select</span>
                <span><kbd className="px-1.5 py-0.5 bg-slate-100 border border-slate-200 rounded">←</kbd><kbd className="px-1.5 py-0.5 bg-slate-100 border border-slate-200 rounded">→</kbd> Navigate</span>
                <span><kbd className="px-1.5 py-0.5 bg-slate-100 border border-slate-200 rounded">M</kbd> Review</span>
              </div>
            </div>
          </div>

          {/* Right Sidebar: Navigator (1 col) */}
          <div className="lg:col-span-1 space-y-4">
            <QuestionNavigator
              totalQuestions={testDetail.questions.length}
              currentIndex={currentIndex}
              answers={answers}
              questionIds={questionIds}
              markedForReview={markedForReview}
              onSelectQuestion={(idx) => setCurrentIndex(idx)}
            />
          </div>
        </div>
      </main>

      {/* Submission Confirmation Modal */}
      <SubmissionConfirmModal
        isOpen={showSubmitModal}
        onClose={() => setShowSubmitModal(false)}
        onConfirm={handleSubmit}
        totalQuestions={testDetail.questions.length}
        answeredCount={answeredCount}
        unansweredCount={unansweredCount}
        isSubmitting={isSubmitting}
      />
    </div>
  );
}
