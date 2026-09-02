"use client";

import React, { useState } from "react";
import { Topic } from "@/types/topic";
import { Search, Check, Info, X } from "lucide-react";
import { Badge } from "@/components/ui/Badge";

interface TopicSelectorProps {
  topics: Topic[];
  selectedTopics: string[];
  onChange: (topics: string[]) => void;
  selectedSubTopics: string[];
  onSubTopicsChange: (subTopics: string[]) => void;
}

export function TopicSelector({
  topics,
  selectedTopics,
  onChange,
  selectedSubTopics,
  onSubTopicsChange,
}: TopicSelectorProps) {
  const [search, setSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("All");
  const [activeSubTopicDrawer, setActiveSubTopicDrawer] = useState<Topic | null>(null);

  const categories = ["All", ...Array.from(new Set(topics.map((t) => t.category)))];

  const filteredTopics = topics.filter((t) => {
    const matchesSearch = t.name.toLowerCase().includes(search.toLowerCase()) ||
      t.description.toLowerCase().includes(search.toLowerCase());
    const matchesCat = categoryFilter === "All" || t.category === categoryFilter;
    return matchesSearch && matchesCat;
  });

  const toggleTopic = (topicName: string) => {
    if (selectedTopics.includes(topicName)) {
      onChange(selectedTopics.filter((t) => t !== topicName));
    } else {
      onChange([...selectedTopics, topicName]);
    }
  };

  const toggleSubTopic = (subId: string) => {
    if (selectedSubTopics.includes(subId)) {
      onSubTopicsChange(selectedSubTopics.filter((s) => s !== subId));
    } else {
      onSubTopicsChange([...selectedSubTopics, subId]);
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <label className="text-sm font-bold text-slate-900 block">
            Select Technical Topics <span className="text-rose-500">*</span>
          </label>
          <p className="text-xs text-slate-500 mt-0.5">
            Choose one or more topics to build your personalized technical assessment.
          </p>
        </div>

        {/* Selected count badge */}
        <div className="flex items-center gap-2">
          {selectedTopics.length > 0 && (
            <Badge variant="purple" size="md">
              {selectedTopics.length} topic{selectedTopics.length > 1 ? "s" : ""} selected
            </Badge>
          )}
          {selectedTopics.length > 0 && (
            <button
              type="button"
              onClick={() => onChange([])}
              className="text-xs text-slate-400 hover:text-slate-600 underline"
            >
              Clear
            </button>
          )}
        </div>
      </div>

      {/* Search & Category Filter Bar */}
      <div className="flex flex-col sm:flex-row gap-2">
        <div className="relative flex-1">
          <Search className="h-4 w-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search topics (e.g. Streams, Spring, SQL, React)..."
            className="w-full pl-9 pr-4 py-2 text-xs rounded-lg border border-slate-200 bg-white placeholder-slate-400 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500"
          />
          {search && (
            <button
              onClick={() => setSearch("")}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <X className="h-3.5 w-3.5" />
            </button>
          )}
        </div>

        <div className="flex items-center gap-1 overflow-x-auto pb-1 sm:pb-0">
          {categories.map((cat) => (
            <button
              key={cat}
              type="button"
              onClick={() => setCategoryFilter(cat)}
              className={`px-2.5 py-1.5 text-xs font-medium rounded-lg whitespace-nowrap transition-colors ${
                categoryFilter === cat
                  ? "bg-slate-900 text-white"
                  : "bg-slate-100 text-slate-600 hover:bg-slate-200"
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Topics selection grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-2.5 max-h-80 overflow-y-auto pr-1">
        {filteredTopics.map((topic) => {
          const isSelected = selectedTopics.includes(topic.name);
          return (
            <div
              key={topic.id}
              onClick={() => toggleTopic(topic.name)}
              className={`flex items-start justify-between p-3 rounded-xl border text-left cursor-pointer transition-all duration-150 select-none ${
                isSelected
                  ? "bg-indigo-50/70 border-indigo-500 shadow-xs ring-1 ring-indigo-500"
                  : "bg-white border-slate-200 hover:border-slate-300 hover:bg-slate-50/50"
              }`}
            >
              <div className="flex-1 pr-2">
                <div className="flex items-center gap-1.5">
                  <span className={`text-xs font-bold ${isSelected ? "text-indigo-900" : "text-slate-800"}`}>
                    {topic.name}
                  </span>
                  {topic.popular && <Badge variant="warning" size="sm">Hot</Badge>}
                </div>
                <p className="text-[11px] text-slate-500 line-clamp-1 mt-0.5">
                  {topic.description}
                </p>

                {/* Subtopic trigger */}
                {topic.subTopics?.length > 0 && (
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      setActiveSubTopicDrawer(topic);
                    }}
                    className="mt-1.5 inline-flex items-center gap-1 text-[10px] text-indigo-600 hover:text-indigo-800 font-medium"
                  >
                    <Info className="h-3 w-3" />
                    <span>{topic.subTopics.length} subtopics</span>
                  </button>
                )}
              </div>

              <div
                className={`h-5 w-5 rounded-md flex items-center justify-center shrink-0 mt-0.5 border transition-colors ${
                  isSelected
                    ? "bg-indigo-600 border-indigo-600 text-white"
                    : "border-slate-300 bg-white"
                }`}
              >
                {isSelected && <Check className="h-3.5 w-3.5 stroke-[3]" />}
              </div>
            </div>
          );
        })}
      </div>

      {/* Subtopics Modal/Drawer */}
      {activeSubTopicDrawer && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-slate-200">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div>
                <h4 className="font-bold text-slate-900 text-base">{activeSubTopicDrawer.name}</h4>
                <p className="text-xs text-slate-500">Fine-tune specific focus areas</p>
              </div>
              <button
                onClick={() => setActiveSubTopicDrawer(null)}
                className="h-8 w-8 rounded-full flex items-center justify-center text-slate-400 hover:text-slate-700 hover:bg-slate-100"
              >
                <X className="h-4 w-4" />
              </button>
            </div>

            <div className="space-y-2 mt-4 max-h-64 overflow-y-auto pr-1">
              {activeSubTopicDrawer.subTopics.map((sub) => {
                const isChecked = selectedSubTopics.includes(sub.id);
                return (
                  <div
                    key={sub.id}
                    onClick={() => toggleSubTopic(sub.id)}
                    className={`flex items-start justify-between p-3 rounded-lg border cursor-pointer text-xs transition-colors ${
                      isChecked
                        ? "bg-indigo-50 border-indigo-400 text-indigo-900"
                        : "bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100"
                    }`}
                  >
                    <div>
                      <p className="font-semibold">{sub.name}</p>
                      <p className="text-slate-500 text-[11px] mt-0.5">{sub.description}</p>
                    </div>
                    <div className={`h-4 w-4 rounded flex items-center justify-center border shrink-0 mt-0.5 ${
                      isChecked ? "bg-indigo-600 border-indigo-600 text-white" : "border-slate-300 bg-white"
                    }`}>
                      {isChecked && <Check className="h-3 w-3 stroke-[3]" />}
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="mt-6 flex justify-end">
              <button
                type="button"
                onClick={() => setActiveSubTopicDrawer(null)}
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold rounded-lg"
              >
                Done
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
