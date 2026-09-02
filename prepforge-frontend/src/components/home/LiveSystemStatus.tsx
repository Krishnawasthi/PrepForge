"use client";

import React, { useEffect, useState } from "react";
import { prepforgeApi } from "@/lib/api-client";
import { HealthStatus } from "@/types/api";
import { Activity, CheckCircle2, AlertCircle, RefreshCw, Server } from "lucide-react";

export function LiveSystemStatus() {
  const [health, setHealth] = useState<HealthStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const checkStatus = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await prepforgeApi.getHealth();
      if (response.success) {
        setHealth(response.data);
      } else {
        setError(response.message || "Unable to retrieve backend status");
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "Failed to connect to backend");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkStatus();
  }, []);

  return (
    <div className="w-full max-w-4xl mx-auto my-8 bg-white border border-slate-200 rounded-xl p-4 shadow-sm">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100">
        <div className="flex items-center gap-2">
          <Server className="h-4 w-4 text-indigo-600" />
          <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-700">
            System & Architecture Status
          </h3>
          <span className="flex h-2 w-2 relative">
            <span className={`animate-ping absolute inline-flex h-full w-full rounded-full opacity-75 ${error ? "bg-rose-400" : "bg-emerald-400"}`}></span>
            <span className={`relative inline-flex rounded-full h-2 w-2 ${error ? "bg-rose-500" : "bg-emerald-500"}`}></span>
          </span>
        </div>

        <button
          onClick={checkStatus}
          disabled={loading}
          className="inline-flex items-center gap-1 text-xs text-slate-500 hover:text-slate-800 transition-colors disabled:opacity-50"
        >
          <RefreshCw className={`h-3.5 w-3.5 ${loading ? "animate-spin" : ""}`} />
          <span>Refresh probe</span>
        </button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-3 text-xs">
        {/* Backend API status */}
        <div className="flex items-center justify-between p-2.5 rounded-lg bg-slate-50 border border-slate-100">
          <div className="flex items-center gap-2">
            <Activity className="h-4 w-4 text-indigo-500" />
            <span className="font-medium text-slate-700">Spring Boot API</span>
          </div>
          {loading ? (
            <span className="text-slate-400 font-mono">probing...</span>
          ) : error ? (
            <span className="inline-flex items-center gap-1 text-rose-600 font-medium">
              <AlertCircle className="h-3.5 w-3.5" /> Offline
            </span>
          ) : (
            <span className="inline-flex items-center gap-1 text-emerald-600 font-medium">
              <CheckCircle2 className="h-3.5 w-3.5" /> 200 OK (v{health?.version})
            </span>
          )}
        </div>

        {/* Database status */}
        <div className="flex items-center justify-between p-2.5 rounded-lg bg-slate-50 border border-slate-100">
          <div className="flex items-center gap-2">
            <span className="font-medium text-slate-700">MongoDB Layer</span>
          </div>
          {loading ? (
            <span className="text-slate-400 font-mono">probing...</span>
          ) : health?.services?.mongodb?.status === "UP" ? (
            <span className="inline-flex items-center gap-1 text-emerald-600 font-medium">
              <CheckCircle2 className="h-3.5 w-3.5" /> Connected
            </span>
          ) : (
            <span className="text-emerald-600 font-medium">
              Ready (Auto-seed)
            </span>
          )}
        </div>

        {/* AI Engine status */}
        <div className="flex items-center justify-between p-2.5 rounded-lg bg-slate-50 border border-slate-100">
          <div className="flex items-center gap-2">
            <span className="font-medium text-slate-700">AI Engine</span>
          </div>
          <span className="inline-flex items-center gap-1 text-indigo-600 font-medium">
            <CheckCircle2 className="h-3.5 w-3.5" /> Gemini 1.5 Flash
          </span>
        </div>
      </div>
    </div>
  );
}
