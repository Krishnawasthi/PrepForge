import React from "react";
import { cn } from "@/lib/utils";

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  hoverable?: boolean;
}

export function Card({ className, hoverable = false, children, ...props }: CardProps) {
  return (
    <div
      className={cn(
        "bg-white rounded-xl border border-slate-200/80 shadow-sm p-6 transition-all duration-200",
        hoverable && "hover:border-slate-300 hover:shadow-md hover:-translate-y-0.5",
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
}
