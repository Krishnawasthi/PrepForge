import { ApiResponse, HealthStatus } from "@/types/api";
import { Topic } from "@/types/topic";
import { PromptInterpretation, TestConfig, TestDetail, TestResult, TestSubmission } from "@/types/test";
import { getAnonymousSessionId, getCustomApiKey } from "./session";

const API_BASE_URL = typeof window !== "undefined" ? "" : (process.env.BACKEND_API_URL || "http://localhost:8080");

async function fetchApi<T>(endpoint: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
  const sessionId = getAnonymousSessionId();
  const customApiKey = getCustomApiKey();
  const url = `${API_BASE_URL}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;

  const defaultHeaders: Record<string, string> = {
    "Content-Type": "application/json",
    "Accept": "application/json",
    "X-Session-Id": sessionId,
  };

  if (customApiKey) {
    defaultHeaders["X-Gemini-Key"] = customApiKey;
  }

  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers,
      },
      next: { revalidate: 0 },
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(
        errorData?.message || `API request failed with status: ${response.status} ${response.statusText}`
      );
    }

    return await response.json();
  } catch (error: unknown) {
    if (error instanceof Error) {
      throw error;
    }
    throw new Error("An unexpected network error occurred while communicating with PrepForge backend.");
  }
}

export const prepforgeApi = {
  // System Health
  getHealth: () => fetchApi<HealthStatus>("/api/health"),

  // Topics Catalog
  getTopics: (category?: string) => {
    const query = category ? `?category=${encodeURIComponent(category)}` : "";
    return fetchApi<Topic[]>(`/api/topics${query}`);
  },

  getTopicBySlug: (slug: string) => fetchApi<Topic>(`/api/topics/${slug}`),

  // Test Builder & Interpretation
  interpretPrompt: (prompt: string) =>
    fetchApi<PromptInterpretation>("/api/tests/interpret", {
      method: "POST",
      body: JSON.stringify({ prompt }),
    }),

  validateTestConfig: (config: TestConfig) =>
    fetchApi<{ testId: string }>("/api/tests/validate", {
      method: "POST",
      body: JSON.stringify({
        ...config,
        anonymousSessionId: getAnonymousSessionId(),
      }),
    }),

  // Test Execution
  generateTest: (config: TestConfig) =>
    fetchApi<TestDetail>("/api/tests/generate", {
      method: "POST",
      body: JSON.stringify({
        ...config,
        anonymousSessionId: getAnonymousSessionId(),
      }),
    }),

  getTestDetail: (testId: string) =>
    fetchApi<TestDetail>(`/api/tests/${testId}`),

  // Test Submission & Results
  submitTest: (testId: string, submission: TestSubmission) =>
    fetchApi<TestResult>(`/api/tests/${testId}/submit`, {
      method: "POST",
      body: JSON.stringify({
        ...submission,
        anonymousSessionId: getAnonymousSessionId(),
      }),
    }),

  getAttemptResult: (attemptId: string) =>
    fetchApi<TestResult>(`/api/attempts/${attemptId}`),

  // Weak Area Focus Generation
  generateWeakAreaPractice: (weakTopics: string[], questionCount: number = 10) =>
    fetchApi<TestDetail>("/api/tests/weak-area-practice", {
      method: "POST",
      body: JSON.stringify({
        anonymousSessionId: getAnonymousSessionId(),
        weakTopics,
        questionCount,
      }),
    }),
};
