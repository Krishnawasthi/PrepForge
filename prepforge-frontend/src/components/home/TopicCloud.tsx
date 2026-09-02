"use client";

import React, { useEffect, useState } from "react";
import { prepforgeApi } from "@/lib/api-client";
import { Topic } from "@/types/topic";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { 
  Code2, 
  Layers, 
  Boxes, 
  Zap, 
  Workflow, 
  Cpu, 
  AlertTriangle, 
  Server, 
  Leaf, 
  Shield, 
  Globe, 
  Database, 
  Table, 
  HardDrive, 
  Radio,
  BookOpen,
  Lock,
  Network,
  Sparkles,
  Coffee
} from "lucide-react";

// Helper to map topic slug/icon name to Lucide Icon component
const getIconComponent = (icon: string) => {
  switch (icon) {
    case "coffee":
      return Coffee;
    case "layers":
      return Layers;
    case "boxes":
      return Boxes;
    case "zap":
      return Zap;
    case "workflow":
      return Workflow;
    case "cpu":
      return Cpu;
    case "alert-triangle":
      return AlertTriangle;
    case "server":
      return Server;
    case "leaf":
      return Leaf;
    case "shield":
      return Shield;
    case "lock":
      return Lock;
    case "network":
      return Network;
    case "globe":
      return Globe;
    case "database":
      return Database;
    case "table":
      return Table;
    case "hard-drive":
      return HardDrive;
    case "radio":
      return Radio;
    default:
      return Code2;
  }
};

export function TopicCloud() {
  const [topics, setTopics] = useState<Topic[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>("All");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadTopics() {
      try {
        setLoading(true);
        const response = await prepforgeApi.getTopics();
        if (response.success) {
          setTopics(response.data);
        } else {
          setError(response.message || "Failed to load topic catalog");
        }
      } catch (err: unknown) {
        setError(err instanceof Error ? err.message : "Error connecting to backend topics catalog");
      } finally {
        setLoading(false);
      }
    }
    loadTopics();
  }, []);

  const categories = ["All", ...Array.from(new Set(topics.map((t) => t.category)))];

  const filteredTopics = selectedCategory === "All"
    ? topics
    : topics.filter((t) => t.category === selectedCategory);

  return (
    <section id="topics" className="py-12 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="text-center max-w-2xl mx-auto mb-8">
        <div className="inline-flex items-center gap-1.5 text-xs font-semibold text-indigo-600 bg-indigo-50 px-2.5 py-1 rounded-full mb-3">
          <Sparkles className="h-3.5 w-3.5" />
          <span>Java Backend Engineering Curriculum</span>
        </div>
        <h2 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">
          Curated Java Backend Assessment Tracks
        </h2>
        <p className="mt-2 text-sm text-slate-600">
          Master the exact technical interview loops asked by top tech firms: Core Java, Concurrency, Spring Boot, Spring Security, Microservices, and REST API Design.
        </p>
      </div>

      {/* Category Tabs */}
      <div className="flex flex-wrap items-center justify-center gap-1.5 mb-8">
        {categories.map((cat) => (
          <button
            key={cat}
            onClick={() => setSelectedCategory(cat)}
            className={`px-3.5 py-1.5 text-xs font-medium rounded-full transition-all duration-150 ${
              selectedCategory === cat
                ? "bg-indigo-600 text-white shadow-sm"
                : "bg-white text-slate-600 border border-slate-200 hover:bg-slate-50 hover:text-slate-900"
            }`}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Loading & Error States */}
      {loading && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="h-44 rounded-xl bg-slate-100 animate-pulse border border-slate-200/60 p-5"></div>
          ))}
        </div>
      )}

      {error && (
        <div className="text-center p-8 bg-rose-50 border border-rose-200 rounded-xl max-w-md mx-auto">
          <p className="text-sm font-semibold text-rose-800">Connection Note</p>
          <p className="text-xs text-rose-600 mt-1">{error}</p>
        </div>
      )}

      {/* Topics Grid */}
      {!loading && !error && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredTopics.map((topic) => {
            const Icon = getIconComponent(topic.icon);
            return (
              <Card key={topic.id} hoverable className="flex flex-col justify-between h-full bg-white group border-slate-200">
                <div>
                  <div className="flex items-start justify-between gap-3 mb-3">
                    <div className="h-9 w-9 rounded-lg bg-indigo-50 border border-indigo-100 flex items-center justify-center text-indigo-600 group-hover:bg-indigo-600 group-hover:text-white transition-colors">
                      <Icon className="h-5 w-5" />
                    </div>
                    <div className="flex items-center gap-1.5">
                      {topic.popular && (
                        <Badge variant="warning" size="sm">Popular</Badge>
                      )}
                      <Badge variant="neutral" size="sm">{topic.category}</Badge>
                    </div>
                  </div>

                  <h3 className="text-base font-bold text-slate-900 tracking-tight mb-1 group-hover:text-indigo-600 transition-colors">
                    {topic.name}
                  </h3>

                  <p className="text-xs text-slate-500 leading-relaxed line-clamp-2 mb-4">
                    {topic.description}
                  </p>
                </div>

                {/* Subtopic pill tags */}
                <div>
                  <div className="pt-3 border-t border-slate-100 flex flex-wrap gap-1">
                    {topic.subTopics?.slice(0, 2).map((sub) => (
                      <span key={sub.id} className="text-[11px] bg-slate-50 border border-slate-200/60 text-slate-600 px-2 py-0.5 rounded-md">
                        {sub.name}
                      </span>
                    ))}
                    {(topic.subTopics?.length || 0) > 2 && (
                      <span className="text-[11px] text-slate-400 px-1 py-0.5 font-medium">
                        +{(topic.subTopics?.length || 0) - 2} more
                      </span>
                    )}
                  </div>
                </div>
              </Card>
            );
          })}
        </div>
      )}
    </section>
  );
}
