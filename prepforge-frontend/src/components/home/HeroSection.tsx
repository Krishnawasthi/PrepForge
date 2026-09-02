import React from "react";
import Link from "next/link";
import { Sparkles, ArrowRight, Shield, Layers, Flame, Coffee } from "lucide-react";
import { Badge } from "@/components/ui/Badge";

export function HeroSection() {
  return (
    <section className="relative pt-12 pb-8 overflow-hidden">
      {/* Background subtle radial gradient */}
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(120,119,198,0.12),rgba(255,255,255,0))] -z-10" />

      <div className="max-w-4xl mx-auto text-center px-4">
        {/* Value Tagline Badge */}
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-50/80 border border-indigo-100 text-indigo-700 text-xs font-semibold mb-6 shadow-sm">
          <Coffee className="h-3.5 w-3.5 text-amber-600" />
          <span>AI-Powered Java Backend Interview Platform</span>
          <span className="text-slate-300">•</span>
          <span className="text-slate-600 font-normal">Zero Sign-up Required</span>
        </div>

        {/* Main Headline */}
        <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-slate-900 leading-[1.15]">
          Practice smarter. <br />
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-violet-600">
            Ace Your Java Backend Interview.
          </span>
        </h1>

        {/* Subtitle */}
        <p className="mt-5 text-base sm:text-lg text-slate-600 max-w-2xl mx-auto leading-relaxed">
          Generate targeted, high-yield Java backend technical tests tailored to your exact experience level (0–5+ YOE), Spring Boot mastery, Core Java, and Concurrency depth in seconds. Powered by Gemini AI.
        </p>

        {/* Natural Language Prompt Preview Banner */}
        <div className="mt-8 bg-slate-900 text-slate-100 rounded-2xl p-5 text-left shadow-xl border border-slate-800 max-w-2xl mx-auto relative overflow-hidden">
          <div className="flex items-center justify-between pb-3 border-b border-slate-800 text-xs text-slate-400">
            <div className="flex items-center gap-2">
              <span className="h-2.5 w-2.5 rounded-full bg-rose-500 inline-block"></span>
              <span className="h-2.5 w-2.5 rounded-full bg-amber-500 inline-block"></span>
              <span className="h-2.5 w-2.5 rounded-full bg-emerald-500 inline-block"></span>
              <span className="font-mono ml-2 text-slate-400">java-backend-prompt.ai</span>
            </div>
            <span className="text-[11px] bg-slate-800 px-2 py-0.5 rounded text-indigo-300 font-mono">Natural Language Builder</span>
          </div>

          <p className="mt-3 text-sm text-slate-300 leading-relaxed font-mono">
            &quot;I have 1.5 years of Java experience and I&apos;m preparing for a backend developer interview. Give me medium to hard questions focused on Collections, Multithreading, Java 8 Streams and OOP. Include output-based and tricky interview questions.&quot;
          </p>

          <div className="mt-4 pt-3 border-t border-slate-800/80 flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-2">
              <Badge variant="purple" size="sm">Core Java</Badge>
              <Badge variant="info" size="sm">Collections</Badge>
              <Badge variant="warning" size="sm">1.5 YOE</Badge>
              <Badge variant="default" size="sm">Medium/Hard</Badge>
            </div>
            <Link
              href="#topics"
              className="inline-flex items-center gap-1.5 text-xs font-semibold text-indigo-400 hover:text-indigo-300 transition-colors"
            >
              <span>Explore Java Tracks</span>
              <ArrowRight className="h-3.5 w-3.5" />
            </Link>
          </div>
        </div>

        {/* Value props */}
        <div className="mt-10 grid grid-cols-1 sm:grid-cols-3 gap-4 max-w-2xl mx-auto text-left text-xs">
          <div className="flex items-start gap-2.5 p-3 rounded-lg bg-white border border-slate-200/80 shadow-sm">
            <Shield className="h-4 w-4 text-emerald-600 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-slate-800">100% Anonymous</p>
              <p className="text-slate-500 mt-0.5">No login, email, or credentials collected.</p>
            </div>
          </div>

          <div className="flex items-start gap-2.5 p-3 rounded-lg bg-white border border-slate-200/80 shadow-sm">
            <Flame className="h-4 w-4 text-amber-600 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-slate-800">Java Enterprise Depth</p>
              <p className="text-slate-500 mt-0.5">Spring Boot, Concurrency, JPA & Kafka MCQs.</p>
            </div>
          </div>

          <div className="flex items-start gap-2.5 p-3 rounded-lg bg-white border border-slate-200/80 shadow-sm">
            <Layers className="h-4 w-4 text-indigo-600 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-slate-800">500+ Concurrent Scale</p>
              <p className="text-slate-500 mt-0.5">Stateless Spring Boot enterprise backend.</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
