import React from "react";
import { Sliders, CheckSquare, BarChart3, HelpCircle, TrendingUp, RefreshCw } from "lucide-react";
import { Card } from "@/components/ui/Card";

export function FeaturesSection() {
  const steps = [
    {
      step: "01",
      title: "Configure",
      desc: "Customize topics, experience (0-5+ YOE), difficulty (Easy to Adaptive), question types, or describe via natural language.",
      icon: Sliders,
      color: "text-blue-600 bg-blue-50 border-blue-100",
    },
    {
      step: "02",
      title: "Practice",
      desc: "Timed examination interface with responsive navigation, question review flags, and real-time state tracking.",
      icon: CheckSquare,
      color: "text-indigo-600 bg-indigo-50 border-indigo-100",
    },
    {
      step: "03",
      title: "Evaluate",
      desc: "Stateless backend scoring engine with topic accuracy, difficulty breakdown, and time metrics calculation.",
      icon: BarChart3,
      color: "text-teal-600 bg-teal-50 border-teal-100",
    },
    {
      step: "04",
      title: "Understand",
      desc: "Full breakdown of why the correct option is right, why other options are wrong, and company interview tips.",
      icon: HelpCircle,
      color: "text-purple-600 bg-purple-50 border-purple-100",
    },
    {
      step: "05",
      title: "Improve",
      desc: "Automated weak-area detection pinpointing concepts that need growth before your next interview.",
      icon: TrendingUp,
      color: "text-emerald-600 bg-emerald-50 border-emerald-100",
    },
    {
      step: "06",
      title: "Practice Again",
      desc: "One-click 'Practice Weak Areas' or generate a fresh non-repetitive randomized test set instantly.",
      icon: RefreshCw,
      color: "text-amber-600 bg-amber-50 border-amber-100",
    },
  ];

  return (
    <section className="py-16 bg-slate-50/70 border-y border-slate-200/80 my-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-12">
          <h2 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">
            The PrepForge Practice Loop
          </h2>
          <p className="mt-2 text-sm text-slate-600">
            A continuous mastery flywheel designed to elevate your technical interview readiness with zero friction.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {steps.map((item) => {
            const Icon = item.icon;
            return (
              <Card key={item.step} className="bg-white relative overflow-hidden border-slate-200">
                <span className="absolute top-4 right-4 text-2xl font-mono font-bold text-slate-100 select-none">
                  {item.step}
                </span>
                <div className={`h-10 w-10 rounded-lg flex items-center justify-center border ${item.color} mb-4`}>
                  <Icon className="h-5 w-5" />
                </div>
                <h3 className="text-base font-bold text-slate-900 mb-1.5">{item.title}</h3>
                <p className="text-xs text-slate-600 leading-relaxed">{item.desc}</p>
              </Card>
            );
          })}
        </div>
      </div>
    </section>
  );
}
