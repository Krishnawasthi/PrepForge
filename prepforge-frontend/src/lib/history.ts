/**
 * Anonymous Local Test History Manager
 * Stores recent test attempts in the user's browser localStorage without requiring an account (Requirement #35 & #36).
 */
export interface LocalHistoryItem {
  attemptId: string;
  testId: string;
  testTitle: string;
  score: number;
  totalQuestions: number;
  percentage: number;
  timeTakenSeconds: number;
  completedAt: string;
  weakAreas: string[];
  strongAreas: string[];
}

const STORAGE_KEY = "prepforge_local_history_v1";

export function saveAttemptToLocalHistory(item: LocalHistoryItem): void {
  if (typeof window === "undefined") return;

  try {
    const existing = getLocalHistory();
    // Filter out if duplicate attempt exists
    const filtered = existing.filter((h) => h.attemptId !== item.attemptId);
    const updated = [item, ...filtered].slice(0, 30); // Keep last 30 tests
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
  } catch (err) {
    console.error("Failed to save attempt to localStorage:", err);
  }
}

export function getLocalHistory(): LocalHistoryItem[] {
  if (typeof window === "undefined") return [];

  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    return JSON.parse(raw);
  } catch (err) {
    console.error("Failed to parse local history:", err);
    return [];
  }
}

export function clearLocalHistory(): void {
  if (typeof window === "undefined") return;
  localStorage.removeItem(STORAGE_KEY);
}

export function computeDashboardStats(history: LocalHistoryItem[]) {
  if (history.length === 0) {
    return {
      testsCompleted: 0,
      questionsSolved: 0,
      bestScore: 0,
      averageScore: 0,
      avgCompletionTimeSeconds: 0,
      accumulatedWeakAreas: [] as string[],
      accumulatedStrongAreas: [] as string[],
    };
  }

  const testsCompleted = history.length;
  const questionsSolved = history.reduce((acc, h) => acc + h.totalQuestions, 0);
  const bestScore = Math.max(...history.map((h) => h.percentage));
  const avgScore = Math.round(history.reduce((acc, h) => acc + h.percentage, 0) / testsCompleted);
  const avgTime = Math.round(history.reduce((acc, h) => acc + h.timeTakenSeconds, 0) / testsCompleted);

  // Aggregate weak & strong areas
  const weakCount: Record<string, number> = {};
  const strongCount: Record<string, number> = {};

  history.forEach((h) => {
    h.weakAreas?.forEach((w) => {
      weakCount[w] = (weakCount[w] || 0) + 1;
    });
    h.strongAreas?.forEach((s) => {
      strongCount[s] = (strongCount[s] || 0) + 1;
    });
  });

  const accumulatedWeakAreas = Object.keys(weakCount)
    .sort((a, b) => weakCount[b] - weakCount[a])
    .slice(0, 4);

  const accumulatedStrongAreas = Object.keys(strongCount)
    .sort((a, b) => strongCount[b] - strongCount[a])
    .slice(0, 4);

  return {
    testsCompleted,
    questionsSolved,
    bestScore,
    averageScore: avgScore,
    avgCompletionTimeSeconds: avgTime,
    accumulatedWeakAreas,
    accumulatedStrongAreas,
  };
}
