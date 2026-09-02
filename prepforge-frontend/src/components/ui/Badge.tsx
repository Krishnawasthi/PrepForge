import React from "react";
import { cn } from "@/lib/utils";

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: "default" | "success" | "warning" | "info" | "purple" | "neutral";
  size?: "sm" | "md";
}

export function Badge({ className, variant = "default", size = "sm", children, ...props }: BadgeProps) {
  const sizeStyles = {
    sm: "px-2 py-0.5 text-xs font-medium",
    md: "px-2.5 py-1 text-xs font-semibold",
  };

  const variantStyles = {
    default: "bg-indigo-50 text-indigo-700 border border-indigo-100",
    success: "bg-emerald-50 text-emerald-700 border border-emerald-100",
    warning: "bg-amber-50 text-amber-700 border border-amber-100",
    info: "bg-sky-50 text-sky-700 border border-sky-100",
    purple: "bg-purple-50 text-purple-700 border border-purple-100",
    neutral: "bg-slate-100 text-slate-700 border border-slate-200",
  };

  return (
    <span
      className={cn("inline-flex items-center gap-1 rounded-full", sizeStyles[size], variantStyles[variant], className)}
      {...props}
    >
      {children}
    </span>
  );
}
