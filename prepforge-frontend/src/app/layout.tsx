import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "PrepForge — Java Interview Practice",
  description: "Practice Java interview questions, get instant results, understand mistakes, and improve.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="scroll-smooth">
      <body className="min-h-screen bg-slate-50 text-slate-900 antialiased selection:bg-indigo-500 selection:text-white">
        {children}
      </body>
    </html>
  );
}
