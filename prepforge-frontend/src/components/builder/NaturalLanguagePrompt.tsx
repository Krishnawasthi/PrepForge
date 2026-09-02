"use client";

import React, { useState } from "react";
import { Sparkles, ArrowRight, CornerDownLeft, Coffee } from "lucide-react";
import { Button } from "@/components/ui/Button";

interface NaturalLanguagePromptProps {
  onInterpret: (prompt: string) => Promise<void>;
  isLoading: boolean;
}

const SAMPLE_PROMPTS = [
  {
    title: "Java Backend 1.5 YOE",
    prompt: "I have 1.5 years of Java experience and I'm preparing for a backend developer interview. Give me medium to hard questions focused on Collections, Multithreading, Java 8 Streams and OOP design patterns. Include output-based and tricky interview questions.",
  },
  {
    title: "Spring Boot & Microservices",
    prompt: "I am a 3-5 years experienced Spring Boot backend engineer. Test my depth in Spring Security JWT, REST API design, Spring AOP, microservice patterns (Eureka, Resilience4j circuit breaker), and Spring Cloud distributed tracing.",
  },
  {
    title: "Concurrency & JVM Performance",
    prompt: "Preparing for a Senior Java Developer role. Focus on ExecutorService, volatile vs Atomic, CompletableFuture, G1/ZGC garbage collectors, memory leaks, class loading, and JIT compiler behaviour.",
  },
  {
    title: "Spring Core & Design Patterns",
    prompt: "Give me tough Spring Framework questions on IoC container internals, bean lifecycle, CGLIB vs JDK dynamic proxies, SOLID principles, and GoF design patterns like Strategy, Factory, Observer, and Builder in Java context.",
  },
];

export function NaturalLanguagePrompt({ onInterpret, isLoading }: NaturalLanguagePromptProps) {
  const [prompt, setPrompt] = useState<string>("");

  const wordCount = prompt.trim() ? prompt.trim().split(/\s+/).length : 0;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (prompt.trim() && !isLoading) {
      onInterpret(prompt);
    }
  };

  return (
    <div className="space-y-6">
      {/* Title & guidance */}
      <div>
        <h3 className="text-lg font-bold text-slate-900 tracking-tight flex items-center gap-2">
          <Sparkles className="h-5 w-5 text-indigo-600" />
          <span>Describe your Java Backend test requirements</span>
        </h3>
        <p className="text-xs text-slate-500 mt-1">
          Tell us your Java experience level, target topics (Spring Boot, Streams, Multithreading, JPA), difficulty, and question preferences in plain English (~100–150 words).
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-3">
        <div className="relative rounded-xl border border-slate-300 bg-white shadow-sm focus-within:border-indigo-500 focus-within:ring-1 focus-within:ring-indigo-500 transition-all">
          <textarea
            rows={5}
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            placeholder="e.g. I have 2 years of experience in Java and I'm preparing for a backend developer interview. Give me medium-hard questions focused on Spring Boot, REST APIs, JPA and SQL. Include practical scenarios and tricky interview questions."
            className="w-full rounded-xl p-4 text-sm text-slate-800 placeholder-slate-400 focus:outline-none resize-none"
            maxLength={1000}
          />

          <div className="flex items-center justify-between px-4 py-2.5 bg-slate-50/70 border-t border-slate-100 rounded-b-xl text-xs">
            <span className={`font-mono ${wordCount > 180 ? "text-rose-600 font-semibold" : "text-slate-500"}`}>
              {wordCount} words <span className="text-slate-400 font-normal">({prompt.length}/1000 chars)</span>
            </span>

            <div className="flex items-center gap-2">
              <Button
                type="submit"
                size="sm"
                disabled={!prompt.trim() || isLoading}
                isLoading={isLoading}
              >
                <span>Interpret with AI</span>
                <CornerDownLeft className="h-3.5 w-3.5" />
              </Button>
            </div>
          </div>
        </div>
      </form>

      {/* Preset sample prompts */}
      <div>
        <p className="text-xs font-semibold text-slate-500 mb-2">Or try a Java Backend preset prompt:</p>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
          {SAMPLE_PROMPTS.map((sample, idx) => (
            <button
              key={idx}
              type="button"
              onClick={() => setPrompt(sample.prompt)}
              className="text-left p-3 rounded-lg border border-slate-200 bg-white hover:border-indigo-300 hover:bg-indigo-50/40 transition-all text-xs group"
            >
              <span className="font-semibold text-slate-800 group-hover:text-indigo-700 flex items-center justify-between">
                {sample.title}
                <ArrowRight className="h-3 w-3 text-slate-400 group-hover:text-indigo-600 transition-transform group-hover:translate-x-0.5" />
              </span>
              <p className="text-slate-500 line-clamp-2 mt-1 leading-relaxed">
                {sample.prompt}
              </p>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
