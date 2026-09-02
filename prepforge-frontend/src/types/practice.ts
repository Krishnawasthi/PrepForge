export type ExperienceLevel = "Beginner" | "Intermediate" | "Advanced";

export interface Question {
  id: string;
  question: string;
  options: string[];
  correctAnswer?: string;
  explanation?: string;
  topic: string;
  difficulty?: string;
  userAnswer?: string;
  isCorrect?: boolean;
  isSkipped?: boolean;
}

export interface PracticeTest {
  testId: string;
  topics: string[];
  experienceLevel: ExperienceLevel;
  questionCount: number;
  timeLimitMinutes: number;
  questions: Question[];
}

export interface PracticeResult {
  attemptId: string;
  testId: string;
  score: number;
  totalQuestions: number;
  percentage: number;
  correctCount: number;
  incorrectCount: number;
  skippedCount: number;
  timeTakenSeconds: number;
  weakTopics: string[];
  revisionTips: string[];
  topicMistakes: Record<string, number>;
  questions: Question[];
  completedAt: string;
}

export interface CreatePracticePayload {
  topics: string[];
  experienceLevel: ExperienceLevel;
  questionCount: number;
}

export interface SubmitPracticePayload {
  answers: Record<string, string>;
  timeTakenSeconds: number;
}
