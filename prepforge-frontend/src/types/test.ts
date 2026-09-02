export type ExperienceLevel = 
  | "Beginner"
  | "0-1 years"
  | "1-2 years"
  | "2-3 years"
  | "3-5 years"
  | "5+ years";

export type DifficultyLevel = 
  | "Easy"
  | "Medium"
  | "Hard"
  | "Mixed"
  | "Adaptive";

export type QuestionType =
  | "Conceptual MCQ"
  | "Output-based"
  | "Code analysis"
  | "Scenario-based"
  | "Debugging"
  | "SQL query/result"
  | "Best-practice"
  | "Interview trick questions";

export interface TestConfig {
  topics: string[];
  subTopics?: string[];
  experienceLevel: ExperienceLevel;
  difficulty: DifficultyLevel;
  questionTypes: QuestionType[];
  questionCount: number;
  timeLimitMinutes: number;
  title?: string;
  promptDescription?: string;
  anonymousSessionId?: string;
}

export interface PromptInterpretation {
  originalPrompt: string;
  goal: string;
  topics: string[];
  subTopics: string[];
  experienceLevel: ExperienceLevel;
  difficulty: DifficultyLevel;
  questionTypes: QuestionType[];
  questionCount: number;
  timeLimitMinutes: number;
  interpretationConfidence: number;
}

export interface Question {
  id: string;
  question: string;
  options: string[];
  correctAnswer?: string | null;
  explanation?: string | null;
  optionExplanations?: Record<string, string> | null;
  topic: string;
  subTopic?: string;
  difficulty: string;
  experienceLevel: string;
  questionType: string;
  interviewTip?: string | null;
}

export interface TestSession {
  id?: string;
  testId: string;
  anonymousSessionId?: string;
  title: string;
  promptDescription?: string;
  topics: string[];
  subTopics?: string[];
  experienceLevel: string;
  difficulty: string;
  questionTypes?: string[];
  questionCount: number;
  timeLimitMinutes: number;
  questionIds?: string[];
  createdAt?: string;
  expiresAt?: string;
}

export interface TestDetail {
  testId: string;
  title: string;
  promptDescription?: string;
  topics: string[];
  experienceLevel: string;
  difficulty: string;
  questionCount: number;
  timeLimitMinutes: number;
  questions: Question[];
}

export interface TestSubmission {
  anonymousSessionId?: string;
  attemptId?: string;
  answers: Record<string, string>; // questionId -> chosen answer
  timeTakenSeconds: number;
}

export interface QuestionResult {
  questionId: string;
  question: string;
  options: string[];
  userAnswer?: string | null;
  correctAnswer: string;
  isCorrect: boolean;
  isSkipped: boolean;
  explanation: string;
  optionExplanations?: Record<string, string>;
  topic: string;
  subTopic?: string;
  difficulty: string;
  questionType: string;
  interviewTip?: string;
}

export interface TestResult {
  attemptId: string;
  testId: string;
  testTitle: string;
  totalQuestions: number;
  correctCount: number;
  incorrectCount: number;
  skippedCount: number;
  score: number;
  percentage: number;
  timeTakenSeconds: number;
  feedbackMessage: string;
  topicAccuracy: Record<string, number>;
  difficultyAccuracy: Record<string, number>;
  questionTypeAccuracy?: Record<string, number>;
  weakAreas: string[];
  strongAreas: string[];
  questions: QuestionResult[];
  completedAt: string;
}
