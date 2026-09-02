import { ApiResponse } from "@/types/api";
import {
  CreatePracticePayload,
  PracticeResult,
  PracticeTest,
  Question,
  SubmitPracticePayload,
} from "@/types/practice";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

async function fetchApi<T>(endpoint: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
  const url = `${API_BASE_URL}${endpoint.startsWith("/") ? endpoint : `/${endpoint}`}`;

  const response = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
      ...(options.headers || {}),
    },
  });

  if (!response.ok) {
    let errorMsg = `API request failed: ${response.status} ${response.statusText}`;
    try {
      const errJson = await response.json();
      if (errJson?.message) errorMsg = errJson.message;
    } catch (_) {}
    throw new Error(errorMsg);
  }

  return await response.json();
}

export const practiceApi = {
  getTopics: () => fetchApi<string[]>("/api/topics"),

  createTest: (payload: CreatePracticePayload) =>
    fetchApi<PracticeTest>("/api/tests", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  getTest: (testId: string) => fetchApi<PracticeTest>(`/api/tests/${testId}`),

  changeQuestion: (
    testId: string,
    questionId: string,
    context?: {
      topic?: string;
      difficulty?: string;
      experienceLevel?: string;
      previouslyUsedQuestions?: string[];
    }
  ) =>
    fetchApi<Question>(`/api/tests/${testId}/questions/${questionId}/change`, {
      method: "POST",
      body: JSON.stringify(context || {}),
    }),

  submitTest: (testId: string, payload: SubmitPracticePayload) =>
    fetchApi<PracticeResult>(`/api/tests/${testId}/submit`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  getAttempt: (attemptId: string) => fetchApi<PracticeResult>(`/api/attempts/${attemptId}`),
};
