"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { Terminal, ShieldCheck, Zap, LayoutDashboard, PlusCircle, Key } from "lucide-react";
import { getAnonymousSessionId, getCustomApiKey } from "@/lib/session";
import { Badge } from "@/components/ui/Badge";
import { AISettingsModal } from "./AISettingsModal";

export function Navbar() {
  const [sessionId, setSessionId] = useState<string>("");
  const [hasCustomKey, setHasCustomKey] = useState<boolean>(false);
  const [showSettings, setShowSettings] = useState<boolean>(false);

  useEffect(() => {
    setSessionId(getAnonymousSessionId());
    setHasCustomKey(!!getCustomApiKey());
  }, [showSettings]);

  return (
    <>
      <header className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-slate-200/80">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Brand Logo & Tagline */}
            <div className="flex items-center gap-6">
              <Link href="/" className="flex items-center gap-2.5 group">
                <div className="h-9 w-9 rounded-lg bg-indigo-600 flex items-center justify-center text-white shadow-md shadow-indigo-200 group-hover:scale-105 transition-transform">
                  <Terminal className="h-5 w-5" />
                </div>
                <div className="flex flex-col">
                  <span className="font-bold text-lg tracking-tight text-slate-900 leading-tight">
                    Prep<span className="text-indigo-600">Forge</span>
                  </span>
                  <span className="text-[11px] font-medium text-slate-500 tracking-wide hidden sm:inline">
                    Java Backend Assessment Platform
                  </span>
                </div>
              </Link>

              <nav className="hidden md:flex items-center gap-1">
                <Link
                  href="/create"
                  className="px-3 py-1.5 text-xs font-semibold text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-lg transition-colors flex items-center gap-1.5"
                >
                  <PlusCircle className="h-3.5 w-3.5 text-indigo-600" />
                  <span>Create Test</span>
                </Link>
                <Link
                  href="/dashboard"
                  className="px-3 py-1.5 text-xs font-semibold text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-lg transition-colors flex items-center gap-1.5"
                >
                  <LayoutDashboard className="h-3.5 w-3.5 text-indigo-600" />
                  <span>Practice Dashboard</span>
                </Link>
                <Link
                  href="/#topics"
                  className="px-3 py-1.5 text-xs font-semibold text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-lg transition-colors"
                >
                  Curriculum
                </Link>
              </nav>
            </div>

            {/* Right CTAs & AI Settings */}
            <div className="flex items-center gap-2 sm:gap-3">
              <button
                type="button"
                onClick={() => setShowSettings(true)}
                className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border text-xs font-semibold transition-colors ${
                  hasCustomKey
                    ? "bg-emerald-50 border-emerald-300 text-emerald-800"
                    : "bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100"
                }`}
                title="Configure Gemini LLM API Key"
              >
                <Key className="h-3.5 w-3.5 text-indigo-600" />
                <span className="hidden sm:inline">{hasCustomKey ? "Gemini Active" : "AI Key"}</span>
              </button>

              <Link
                href="/create"
                className="inline-flex items-center gap-1.5 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 px-3.5 py-2 rounded-lg transition-colors shadow-sm"
              >
                <Zap className="h-3.5 w-3.5" />
                <span>Start Test</span>
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* AI Key Settings Modal */}
      <AISettingsModal
        isOpen={showSettings}
        onClose={() => setShowSettings(false)}
      />
    </>
  );
}
