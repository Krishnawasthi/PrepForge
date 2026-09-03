"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { practiceApi } from "@/lib/api-client";
import { ExperienceLevel } from "@/types/practice";
import { 
  Check, 
  ArrowRight, 
  Sparkles, 
  BookOpen, 
  AlertCircle,
  RotateCw
} from "lucide-react";

const DEFAULT_TOPICS = [
  "Core Java",
  "Object-Oriented Programming (OOP)",
  "Java Collections Framework",
  "Exception Handling",
  "Java 8 & Modern Java",
  "Streams API",
  "Multithreading & Concurrency",
  "JVM Internals & Memory",
  "Strings & Immutability",
  "Generics",
  "Interfaces & Abstract Classes",
  "Inheritance & Polymorphism",
  "Constructors & Object Lifecycle",
  "Encapsulation & Access Modifiers"
];

const EXPERIENCE_OPTIONS: { id: ExperienceLevel; label: string; desc: string }[] = [
  { id: "Beginner", label: "Beginner", desc: "Core syntax, OOP basics, fundamentals" },
  { id: "Intermediate", label: "Intermediate", desc: "Real-world interview problems & idioms" },
  { id: "Advanced", label: "Advanced", desc: "Deep internals, concurrency, optimization" },
];

const QUESTION_COUNTS = [5, 10, 20, 30, 40, 50];

export default function HomePage() {
  const router = useRouter();

  const [topics, setTopics] = useState<string[]>(DEFAULT_TOPICS);
  const [selectedTopics, setSelectedTopics] = useState<string[]>([
    "Core Java",
    "Object-Oriented Programming (OOP)",
    "Java Collections Framework",
    "Streams API",
  ]);
  const [experience, setExperience] = useState<ExperienceLevel>("Intermediate");
  const [questionCount, setQuestionCount] = useState<number>(10);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Load backend topics
  useEffect(() => {
    practiceApi.getTopics()
      .then((res) => {
        if (res.success && res.data && res.data.length > 0) {
          setTopics(res.data);
        }
      })
      .catch(() => {});
  }, []);

  const toggleTopic = (topic: string) => {
    setSelectedTopics((prev) =>
      prev.includes(topic) ? prev.filter((t) => t !== topic) : [...prev, topic]
    );
  };

  const selectAllTopics = () => setSelectedTopics([...topics]);
  const clearTopics = () => setSelectedTopics([]);

  const handleStartPractice = async () => {
    if (selectedTopics.length === 0) {
      setError("Please select at least one Java topic to practice.");
      return;
    }

    setError(null);
    setLoading(true);

    try {
      const res = await practiceApi.createTest({
        topics: selectedTopics,
        experienceLevel: experience,
        questionCount,
      });

      if (res.success && res.data?.testId) {
        router.push(`/test/${res.data.testId}`);
      } else {
        setError(res.message || "Failed to create practice test. Please try again.");
        setLoading(false);
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Error connecting to practice engine.");
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 to-white text-slate-900 flex flex-col justify-between">
      {/* Top Simple Navigation */}
      <header className="border-b border-slate-200/80 bg-white/80 backdrop-blur-md sticky top-0 z-30">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="h-8 w-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-bold text-base shadow-xs">
              ☕
            </div>
            <div>
              <span className="font-extrabold text-base tracking-tight text-slate-900">
                PrepForge
              </span>
              <span className="text-xs text-indigo-600 font-semibold ml-2 px-2 py-0.5 rounded-full bg-indigo-50 border border-indigo-100">
                Java Practice
              </span>
            </div>
          </div>
          <span className="text-xs text-slate-500 font-medium hidden sm:inline">
            Fast • Universal • Instant Results
          </span>
        </div>
      </header>

      {/* Main Practice Setup Container */}
      <main className="max-w-3xl w-full mx-auto px-4 sm:px-6 py-10">
        <div className="text-center space-y-3 mb-10">
          <h1 className="text-3xl sm:text-4xl font-black tracking-tight text-slate-900">
            Java Interview Practice
          </h1>
          <p className="text-sm sm:text-base text-slate-600 max-w-xl mx-auto leading-relaxed">
            Practice Java interview questions, get instant results, understand mistakes, and improve.
          </p>
        </div>

        {/* Error Notification */}
        {error && (
          <div className="mb-6 p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs flex items-center gap-2">
            <AlertCircle className="h-4 w-4 shrink-0 text-rose-600" />
            <span className="font-medium">{error}</span>
          </div>
        )}

        <div className="bg-white border border-slate-200 rounded-2xl shadow-xs p-6 sm:p-8 space-y-8">
          {/* Section 1: Choose Topics */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div>
                <label className="text-sm font-bold text-slate-900 flex items-center gap-1.5">
                  <BookOpen className="h-4 w-4 text-indigo-600" />
                  <span>Choose Topics</span>
                </label>
                <p className="text-xs text-slate-500 mt-0.5">
                  Select the Java concepts you want to focus on ({selectedTopics.length} selected)
                </p>
              </div>
              <div className="flex items-center gap-2 text-xs">
                <button
                  type="button"
                  onClick={selectAllTopics}
                  className="text-indigo-600 hover:text-indigo-800 font-semibold px-2 py-1 rounded hover:bg-indigo-50"
                >
                  Select All
                </button>
                <span className="text-slate-300">|</span>
                <button
                  type="button"
                  onClick={clearTopics}
                  className="text-slate-500 hover:text-slate-700 font-semibold px-2 py-1 rounded hover:bg-slate-100"
                >
                  Clear
                </button>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 pt-1">
              {topics.map((topic) => {
                const isSelected = selectedTopics.includes(topic);
                return (
                  <button
                    type="button"
                    key={topic}
                    onClick={() => toggleTopic(topic)}
                    className={`flex items-center justify-between p-3 rounded-xl border text-left text-xs font-medium transition-all ${
                      isSelected
                        ? "bg-indigo-50/70 border-indigo-500 text-indigo-950 font-semibold ring-1 ring-indigo-500"
                        : "bg-white border-slate-200 text-slate-700 hover:border-slate-300 hover:bg-slate-50/50"
                    }`}
                  >
                    <span>{topic}</span>
                    <div
                      className={`h-4 w-4 rounded flex items-center justify-center border transition-colors ${
                        isSelected
                          ? "bg-indigo-600 border-indigo-600 text-white"
                          : "border-slate-300 bg-white"
                      }`}
                    >
                      {isSelected && <Check className="h-3 w-3 stroke-[3]" />}
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Section 2: Your Experience */}
          <div className="space-y-3 pt-6 border-t border-slate-100">
            <div>
              <label className="text-sm font-bold text-slate-900 block">
                Your Experience
              </label>
              <p className="text-xs text-slate-500 mt-0.5">
                Calibrates question depth without changing your selected topics
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              {EXPERIENCE_OPTIONS.map((item) => {
                const isSelected = experience === item.id;
                return (
                  <button
                    type="button"
                    key={item.id}
                    onClick={() => setExperience(item.id)}
                    className={`p-3.5 rounded-xl border text-left transition-all ${
                      isSelected
                        ? "bg-indigo-50/70 border-indigo-600 ring-1 ring-indigo-600"
                        : "bg-white border-slate-200 hover:border-slate-300 hover:bg-slate-50/50"
                    }`}
                  >
                    <div className="flex items-center justify-between mb-1">
                      <span className={`text-xs font-bold ${isSelected ? "text-indigo-950" : "text-slate-800"}`}>
                        {item.label}
                      </span>
                      <div className={`h-3.5 w-3.5 rounded-full border flex items-center justify-center ${
                        isSelected ? "border-indigo-600 bg-indigo-600" : "border-slate-300"
                      }`}>
                        {isSelected && <div className="h-1.5 w-1.5 rounded-full bg-white" />}
                      </div>
                    </div>
                    <p className="text-[11px] text-slate-500 leading-snug">
                      {item.desc}
                    </p>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Section 3: Number of Questions */}
          <div className="space-y-3 pt-6 border-t border-slate-100">
            <div className="flex items-center justify-between">
              <label className="text-sm font-bold text-slate-900 block">
                Number of Questions
              </label>
              <span className="text-xs font-mono font-bold text-indigo-700 bg-indigo-50 px-2.5 py-0.5 rounded-md border border-indigo-100">
                {questionCount} Questions (~{questionCount * 2} min)
              </span>
            </div>

            <div className="grid grid-cols-3 sm:grid-cols-6 gap-2">
              {QUESTION_COUNTS.map((cnt) => {
                const isSelected = questionCount === cnt;
                return (
                  <button
                    type="button"
                    key={cnt}
                    onClick={() => setQuestionCount(cnt)}
                    className={`py-2.5 rounded-xl border text-center text-xs font-bold transition-all ${
                      isSelected
                        ? "bg-indigo-600 border-indigo-600 text-white shadow-xs"
                        : "bg-white border-slate-200 text-slate-700 hover:border-slate-300 hover:bg-slate-50"
                    }`}
                  >
                    {cnt} Qs
                  </button>
                );
              })}
            </div>
          </div>

          {/* Start Button */}
          <div className="pt-6 border-t border-slate-100">
            <button
              type="button"
              disabled={loading}
              onClick={handleStartPractice}
              className="w-full py-4 px-6 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-sm shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2 disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? (
                <>
                  <RotateCw className="h-4 w-4 animate-spin text-white" />
                  <span>Preparing Your Java Practice Test...</span>
                </>
              ) : (
                <>
                  <Sparkles className="h-4 w-4 text-indigo-200" />
                  <span>Start Practice ({questionCount} Questions)</span>
                  <ArrowRight className="h-4 w-4 ml-1" />
                </>
              )}
            </button>
            <p className="text-center text-[11px] text-slate-400 mt-2">
              Instant generation with automatic difficulty progression • 0 duplicates guaranteed
            </p>
          </div>
        </div>
      </main>

      {/* Clean Footer */}
      <footer className="border-t border-slate-200/80 py-6 text-center text-xs text-slate-500">
        <p>PrepForge — Focused Java Interview Question Platform</p>
      </footer>
    </div>
  );
}
