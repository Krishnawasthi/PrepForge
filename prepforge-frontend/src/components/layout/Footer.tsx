import React from "react";
import { Terminal, Shield, Cpu } from "lucide-react";

export function Footer() {
  return (
    <footer className="border-t border-slate-200 bg-white py-10 mt-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-3">
            <div className="h-7 w-7 rounded-md bg-indigo-600 flex items-center justify-center text-white">
              <Terminal className="h-4 w-4" />
            </div>
            <div>
              <p className="text-sm font-semibold text-slate-800">
                PrepForge — AI-Powered Technical Interview Preparation
              </p>
              <p className="text-xs text-slate-500">
                Practice smarter. Prepare better. Built with Spring Boot & Next.js.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-6 text-xs text-slate-500">
            <span className="flex items-center gap-1">
              <Shield className="h-3.5 w-3.5 text-emerald-600" />
              Privacy-first (Zero PII stored)
            </span>
            <span className="flex items-center gap-1">
              <Cpu className="h-3.5 w-3.5 text-indigo-600" />
              High Concurrency Architecture
            </span>
          </div>
        </div>
      </div>
    </footer>
  );
}
