"use client";

import React, { useState, useEffect } from "react";
import { Sparkles, Key, Check, X, ShieldAlert, Zap } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { getCustomApiKey, setCustomApiKey } from "@/lib/session";

interface AISettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function AISettingsModal({ isOpen, onClose }: AISettingsModalProps) {
  const [apiKey, setApiKey] = useState("");
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setApiKey(getCustomApiKey());
      setSaved(false);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSave = () => {
    setCustomApiKey(apiKey);
    setSaved(true);
    setTimeout(() => {
      onClose();
    }, 800);
  };

  const handleClear = () => {
    setCustomApiKey("");
    setApiKey("");
    setSaved(true);
    setTimeout(() => {
      onClose();
    }, 800);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4 animate-in fade-in duration-150">
      <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-200 space-y-5">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-2.5">
            <div className="h-9 w-9 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center">
              <Key className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">LLM & AI Engine Settings</h3>
              <p className="text-xs text-slate-500">Configure your Google Gemini API Key</p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="text-slate-400 hover:text-slate-700 p-1"
          >
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="space-y-3">
          <div className="p-3 rounded-xl bg-indigo-50/70 border border-indigo-100 text-xs text-indigo-950 leading-relaxed">
            <p className="font-semibold flex items-center gap-1 text-indigo-900 mb-1">
              <Zap className="h-3.5 w-3.5 text-indigo-600" />
              <span>Unlimited Live AI Generation</span>
            </p>
            Add your Gemini API Key to generate completely unique, brand-new Java backend questions on every single test generation.
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-700 block">
              Gemini API Key (Optional)
            </label>
            <input
              type="password"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              placeholder="AIzaSy..."
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-xs font-mono text-slate-800 placeholder-slate-400 focus:outline-none focus:border-indigo-600 focus:ring-1 focus:ring-indigo-600"
            />
            <p className="text-[11px] text-slate-400">
              Stored securely in your browser&apos;s localStorage only. Never shared or stored on external servers.
            </p>
          </div>
        </div>

        {saved && (
          <div className="p-2.5 rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-semibold flex items-center gap-2">
            <Check className="h-4 w-4 text-emerald-600" />
            <span>Settings saved successfully!</span>
          </div>
        )}

        <div className="flex items-center justify-between pt-2 border-t border-slate-100">
          <button
            type="button"
            onClick={handleClear}
            className="text-xs text-slate-400 hover:text-rose-600 font-medium transition-colors"
          >
            Use Built-in Engine
          </button>

          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" onClick={onClose} className="text-xs">
              Cancel
            </Button>
            <Button variant="primary" size="sm" onClick={handleSave} className="text-xs">
              Save API Key
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
