"use client";

import React, { useState } from "react";
import { Check, Copy } from "lucide-react";

interface CodeEditorViewProps {
  code: string;
  language?: string;
  filename?: string;
}

export function CodeEditorView({
  code,
  language = "java",
  filename = "Main.java",
}: CodeEditorViewProps) {
  const [copied, setCopied] = useState(false);

  const cleanCode = code.trim();
  const lines = cleanCode.split("\n");

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(cleanCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (_) {}
  };

  return (
    <div className="my-3.5 rounded-xl overflow-hidden border border-slate-800 bg-[#161822] shadow-lg">
      {/* IDE Window Titlebar */}
      <div className="flex items-center justify-between px-3.5 py-2 bg-[#1e202f] border-b border-slate-800/90 select-none">
        <div className="flex items-center gap-2">
          {/* Mac/IDE Window Dots */}
          <div className="flex items-center gap-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-[#ff5f56] inline-block" />
            <span className="w-2.5 h-2.5 rounded-full bg-[#ffbd2e] inline-block" />
            <span className="w-2.5 h-2.5 rounded-full bg-[#27c93f] inline-block" />
          </div>

          {/* Tab */}
          <div className="ml-2.5 px-2.5 py-0.5 rounded-md bg-[#161822] border border-slate-800/80 text-slate-300 font-mono text-[11px] flex items-center gap-1.5 font-medium">
            <span className="text-amber-400 font-bold">☕</span>
            <span>{filename}</span>
          </div>
        </div>

        {/* Copy button */}
        <button
          type="button"
          onClick={handleCopy}
          className="text-[11px] font-mono font-medium text-slate-400 hover:text-slate-200 transition-colors inline-flex items-center gap-1 px-2 py-0.5 rounded hover:bg-slate-800"
          title="Copy Code"
        >
          {copied ? (
            <>
              <Check className="h-3 w-3 text-emerald-400" />
              <span className="text-emerald-400">Copied</span>
            </>
          ) : (
            <>
              <Copy className="h-3 w-3" />
              <span>Copy</span>
            </>
          )}
        </button>
      </div>

      {/* Code Area with Line Numbers */}
      <div className="p-4 font-mono text-xs sm:text-[13px] leading-relaxed overflow-x-auto flex gap-4 bg-[#161822]">
        {/* Line Numbers */}
        <div className="select-none text-slate-600 text-right font-mono flex flex-col shrink-0 border-r border-slate-800/80 pr-3">
          {lines.map((_, i) => (
            <span key={i} className="leading-relaxed">
              {i + 1}
            </span>
          ))}
        </div>

        {/* Code Lines with Syntax Coloring */}
        <div className="flex-1 whitespace-pre font-mono overflow-x-auto">
          {lines.map((line, i) => (
            <div key={i} className="leading-relaxed">
              {renderHighlightedJavaLine(line)}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

/**
 * Lightweight syntax highlighter for Java code in IDE format.
 */
function renderHighlightedJavaLine(line: string): React.ReactNode {
  if (!line || !line.trim()) return <br />;

  // Comments
  const trimmed = line.trimStart();
  if (trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
    return <span className="text-slate-500 italic">{line}</span>;
  }

  // Java Token Regex matcher
  const tokenRegex =
    /("(?:\\.|[^"\\])*")|(\b(?:public|private|protected|class|interface|enum|extends|implements|static|final|void|int|boolean|double|float|char|byte|short|long|new|return|if|else|for|while|do|switch|case|break|continue|try|catch|finally|throw|throws|import|package|abstract|default|super|this|instanceof|null|true|false)\b)|(\b[A-Z][a-zA-Z0-9_$]*\b)|(\b\d+\b)|(@[a-zA-Z0-9_]+)/g;

  const parts: React.ReactNode[] = [];
  let lastIndex = 0;
  let match: RegExpExecArray | null;

  while ((match = tokenRegex.exec(line)) !== null) {
    if (match.index > lastIndex) {
      parts.push(line.substring(lastIndex, match.index));
    }

    const [fullMatch, str, kw, type, num, annot] = match;

    if (str) {
      parts.push(<span key={match.index} className="text-emerald-300">{str}</span>);
    } else if (kw) {
      parts.push(<span key={match.index} className="text-purple-400 font-semibold">{kw}</span>);
    } else if (type) {
      parts.push(<span key={match.index} className="text-sky-300 font-medium">{type}</span>);
    } else if (num) {
      parts.push(<span key={match.index} className="text-amber-300">{num}</span>);
    } else if (annot) {
      parts.push(<span key={match.index} className="text-yellow-300">{annot}</span>);
    } else {
      parts.push(fullMatch);
    }

    lastIndex = match.index + fullMatch.length;
  }

  if (lastIndex < line.length) {
    parts.push(line.substring(lastIndex));
  }

  return <span className="text-slate-200">{parts}</span>;
}

/**
 * Parses markdown text containing code fences (```java ... ```) and inline code (`...`)
 * and renders formatted text with embedded IDE code blocks.
 */
export function FormattedQuestionText({ text }: { text: string }) {
  if (!text) return null;

  // Split by ``` or ```java or ```java\n
  const fenceRegex = /```(?:java|Java)?\s*([\s\S]*?)```/g;
  const segments: React.ReactNode[] = [];
  let lastIndex = 0;
  let match: RegExpExecArray | null;

  while ((match = fenceRegex.exec(text)) !== null) {
    if (match.index > lastIndex) {
      const textChunk = text.substring(lastIndex, match.index).trim();
      if (textChunk) {
        segments.push(
          <div key={`text-${lastIndex}`} className="leading-relaxed whitespace-pre-line text-slate-900">
            {renderInlineCode(textChunk)}
          </div>
        );
      }
    }

    const codeSnippet = match[1];
    segments.push(
      <CodeEditorView
        key={`code-${match.index}`}
        code={codeSnippet}
        language="java"
      />
    );

    lastIndex = match.index + match[0].length;
  }

  if (lastIndex < text.length) {
    const trailingChunk = text.substring(lastIndex).trim();
    if (trailingChunk) {
      segments.push(
        <div key={`text-${lastIndex}`} className="leading-relaxed whitespace-pre-line text-slate-900 mt-2">
          {renderInlineCode(trailingChunk)}
        </div>
      );
    }
  }

  return <div className="space-y-1">{segments}</div>;
}

/**
 * Renders inline code `sample` with crisp badge styling
 */
function renderInlineCode(content: string): React.ReactNode {
  const parts = content.split(/(`[^`]+`)/g);
  return parts.map((part, i) => {
    if (part.startsWith("`") && part.endsWith("`") && part.length > 2) {
      return (
        <code
          key={i}
          className="font-mono text-[11px] sm:text-xs bg-slate-100 text-indigo-700 px-1.5 py-0.5 rounded border border-slate-200 font-semibold"
        >
          {part.slice(1, -1)}
        </code>
      );
    }
    return part;
  });
}
