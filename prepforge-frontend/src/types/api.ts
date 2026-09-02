export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  error?: Record<string, string> | string | null;
  timestamp: string;
}

export interface HealthStatus {
  status: string;
  applicationName: string;
  version: string;
  environment: string;
  uptimeSeconds: number;
  services: {
    mongodb?: {
      status: string;
      database?: string;
      error?: string;
    };
    aiProvider?: {
      provider: string;
      model: string;
      status: string;
    };
    [key: string]: unknown;
  };
}
