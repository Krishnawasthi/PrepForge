"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { prepforgeApi } from "@/lib/api-client";
import { Topic } from "@/types/topic";
import { 
  DifficultyLevel, 
  ExperienceLevel, 
  PromptInterpretation, 
  QuestionType, 
  TestConfig, 
  TestSession 
} from "@/types/test";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { NaturalLanguagePrompt } from "./NaturalLanguagePrompt";
import { TopicSelector } from "./TopicSelector";
import { ExperienceSelector } from "./ExperienceSelector";
import { DifficultySelector } from "./DifficultySelector";
import { QuestionTypeSelector } from "./QuestionTypeSelector";
import { TestParameters } from "./TestParameters";
import { InterpretedConfigCard } from "./InterpretedConfigCard";
import { LoadingExperience } from "./LoadingExperience";
import { Sparkles, SlidersHorizontal, ArrowRight, AlertCircle } from "lucide-react";

interface TestBuilderProps {
  onTestCreated?: (session: TestSession) => void;
}

export function TestBuilder({ onTestCreated }: TestBuilderProps) {
  const router = useRouter();
  const [mode, setMode] = useState<"ai" | "manual">("ai");
  const [topics, setTopics] = useState<Topic[]>([]);
  const [loadingTopics, setLoadingTopics] = useState(true);

  // Form State
  const [selectedTopics, setSelectedTopics] = useState<string[]>(["Core Java", "Collections"]);
  const [selectedSubTopics, setSelectedSubTopics] = useState<string[]>([]);
  const [experienceLevel, setExperienceLevel] = useState<ExperienceLevel>("1-2 years");
  const [difficulty, setDifficulty] = useState<DifficultyLevel>("Medium");
  const [questionTypes, setQuestionTypes] = useState<QuestionType[]>([
    "Conceptual MCQ",
    "Output-based",
    "Scenario-based",
  ]);
  const [questionCount, setQuestionCount] = useState<number>(10);
  const [timeLimitMinutes, setTimeLimitMinutes] = useState<number>(15);

  // AI Interpretation State
  const [interpreting, setInterpreting] = useState(false);
  const [interpretation, setInterpretation] = useState<PromptInterpretation | null>(null);

  // Generation state
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load available topics on mount
  useEffect(() => {
    async function load() {
      try {
        setLoadingTopics(true);
        const res = await prepforgeApi.getTopics();
        if (res.success) {
          setTopics(res.data);
        }
      } catch (err: unknown) {
        console.error("Failed to load topics for builder:", err);
      } finally {
        setLoadingTopics(false);
      }
    }
    load();
  }, []);

  // Handle Natural Language Prompt Interpretation
  const handleInterpret = async (promptText: string) => {
    setError(null);
    setInterpreting(true);
    try {
      const res = await prepforgeApi.interpretPrompt(promptText);
      if (res.success) {
        setInterpretation(res.data);
        // Sync interpreted values to form state
        setSelectedTopics(res.data.topics);
        setExperienceLevel(res.data.experienceLevel);
        setDifficulty(res.data.difficulty);
        setQuestionTypes(res.data.questionTypes);
        setQuestionCount(res.data.questionCount);
        setTimeLimitMinutes(res.data.timeLimitMinutes);
      } else {
        setError(res.message || "Failed to interpret prompt");
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Error interpreting test requirements");
    } finally {
      setInterpreting(false);
    }
  };

  // Generate full test and navigate directly to test taking interface
  const handleGenerateAndStart = async () => {
    if (selectedTopics.length === 0) {
      setError("Please select at least one technical topic.");
      return;
    }

    setError(null);
    setGenerating(true);

    const config: TestConfig = {
      topics: selectedTopics,
      subTopics: selectedSubTopics,
      experienceLevel,
      difficulty,
      questionTypes,
      questionCount,
      timeLimitMinutes,
      promptDescription: interpretation?.originalPrompt,
    };

    try {
      const res = await prepforgeApi.generateTest(config);
      if (res.success && res.data?.testId) {
        if (onTestCreated) {
          onTestCreated({
            id: res.data.testId,
            testId: res.data.testId,
            anonymousSessionId: "",
            title: res.data.title,
            topics: res.data.topics,
            subTopics: [],
            experienceLevel: res.data.experienceLevel,
            difficulty: res.data.difficulty,
            questionTypes: [],
            questionCount: res.data.questionCount,
            timeLimitMinutes: res.data.timeLimitMinutes,
            questionIds: [],
            createdAt: new Date().toISOString(),
            expiresAt: new Date(Date.now() + 7 * 86400000).toISOString(),
          });
        }
        // Direct jump to active examination
        router.push(`/test/${res.data.testId}`);
      } else {
        setError(res.message || "Failed to initialize assessment questions.");
        setGenerating(false);
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Error creating assessment.");
      setGenerating(false);
    }
  };

  if (generating) {
    return <LoadingExperience title="Building Your Custom Assessment Questions" />;
  }

  return (
    <div id="builder" className="max-w-4xl mx-auto px-4 sm:px-6">
      <Card className="bg-white border-slate-200/90 shadow-lg p-6 sm:p-8 space-y-8">
        {/* Header & Tabs */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-100">
          <div>
            <h2 className="text-xl sm:text-2xl font-extrabold text-slate-900 tracking-tight">
              Create Technical Assessment
            </h2>
            <p className="text-xs sm:text-sm text-slate-500 mt-1">
              Configure parameters or let AI generate a customized test tailored to your interview goals.
            </p>
          </div>

          {/* Builder Mode Toggle */}
          <div className="flex items-center p-1 bg-slate-100 rounded-xl shrink-0">
            <button
              type="button"
              onClick={() => {
                setMode("ai");
                setInterpretation(null);
              }}
              className={`flex items-center gap-1.5 px-3.5 py-1.5 text-xs font-semibold rounded-lg transition-all ${
                mode === "ai"
                  ? "bg-white text-indigo-700 shadow-xs"
                  : "text-slate-600 hover:text-slate-900"
              }`}
            >
              <Sparkles className="h-3.5 w-3.5 text-indigo-600" />
              <span>AI Prompt</span>
            </button>
            <button
              type="button"
              onClick={() => setMode("manual")}
              className={`flex items-center gap-1.5 px-3.5 py-1.5 text-xs font-semibold rounded-lg transition-all ${
                mode === "manual"
                  ? "bg-white text-indigo-700 shadow-xs"
                  : "text-slate-600 hover:text-slate-900"
              }`}
            >
              <SlidersHorizontal className="h-3.5 w-3.5 text-indigo-600" />
              <span>Manual Customizer</span>
            </button>
          </div>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="flex items-center gap-2.5 p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs">
            <AlertCircle className="h-4 w-4 shrink-0 text-rose-600" />
            <span className="font-medium">{error}</span>
          </div>
        )}

        {/* Mode 1: Natural Language Mode */}
        {mode === "ai" && !interpretation && (
          <NaturalLanguagePrompt
            onInterpret={handleInterpret}
            isLoading={interpreting}
          />
        )}

        {/* Mode 1 Sub-state: Interpreted preview card */}
        {mode === "ai" && interpretation && (
          <InterpretedConfigCard
            interpretation={interpretation}
            onEdit={() => setMode("manual")}
            onConfirm={handleGenerateAndStart}
            isLoading={generating}
          />
        )}

        {/* Mode 2: Manual Guided Customizer */}
        {mode === "manual" && (
          <div className="space-y-8">
            <TopicSelector
              topics={topics}
              selectedTopics={selectedTopics}
              onChange={setSelectedTopics}
              selectedSubTopics={selectedSubTopics}
              onSubTopicsChange={setSelectedSubTopics}
            />

            <ExperienceSelector
              value={experienceLevel}
              onChange={setExperienceLevel}
            />

            <DifficultySelector
              value={difficulty}
              onChange={setDifficulty}
            />

            <QuestionTypeSelector
              value={questionTypes}
              onChange={setQuestionTypes}
            />

            <TestParameters
              questionCount={questionCount}
              onQuestionCountChange={setQuestionCount}
              timeLimitMinutes={timeLimitMinutes}
              onTimeLimitChange={setTimeLimitMinutes}
            />

            <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
              <Button
                variant="primary"
                size="lg"
                onClick={handleGenerateAndStart}
                isLoading={generating}
                className="w-full sm:w-auto"
              >
                <span>Generate & Start Assessment ({questionCount} Qs)</span>
                <ArrowRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        )}
      </Card>
    </div>
  );
}
