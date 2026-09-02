/**
 * Anonymous Session & LLM Configuration Management
 * Keeps track of anonymous visitor session ID and optional user-provided Gemini API key in browser local storage without storing PII.
 */
const SESSION_STORAGE_KEY = "prepforge_anon_session_id";
const GEMINI_API_KEY_STORAGE = "prepforge_custom_gemini_key";

export function getAnonymousSessionId(): string {
  if (typeof window === "undefined") {
    return "anon_server";
  }

  let sessionId = localStorage.getItem(SESSION_STORAGE_KEY);
  if (!sessionId) {
    sessionId = `pf_anon_${Date.now()}_${Math.random().toString(36).substring(2, 11)}`;
    localStorage.setItem(SESSION_STORAGE_KEY, sessionId);
  }
  return sessionId;
}

export function getCustomApiKey(): string {
  if (typeof window === "undefined") return "";
  return localStorage.getItem(GEMINI_API_KEY_STORAGE) || "";
}

export function setCustomApiKey(key: string): void {
  if (typeof window === "undefined") return;
  if (!key || !key.trim()) {
    localStorage.removeItem(GEMINI_API_KEY_STORAGE);
  } else {
    localStorage.setItem(GEMINI_API_KEY_STORAGE, key.trim());
  }
}
