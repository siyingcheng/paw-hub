export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}

export interface AuthResponse {
  token: string;
  userId: number;
  username: string;
}

export interface TestRun {
  id: number;
  projectId: number;
  runIdentifier: string;
  branch?: string;
  commitSha?: string;
  triggeredBy?: string;
  environment: string;
  totalCases: number;
  passed: number;
  failed: number;
  skipped: number;
  durationMs: number;
  status: 'PASS' | 'FAIL' | 'ERROR';
  createdAt: string;
}

export interface TestExecution {
  id: number;
  attempt: number;
  suiteName: string;
  className: string;
  testName: string;
  caseNumber?: string;
  status: 'PASS' | 'FAIL' | 'SKIP' | 'ERROR';
  durationMs: number;
  errorMessage?: string;
  errorType?: string;
  stackTrace?: string;
}

export interface TrendDataPoint {
  date: string;
  passRate: number;
  failureRate: number;
  avgDurationMs: number;
  retryRate: number;
}

export interface TrendResponse {
  environment: string;
  periodType: string;
  dataPoints: TrendDataPoint[];
}

export interface FlakyTest {
  testCaseKey: string;
  flakyScore: number;
  transitionCount: number;
  retryPassCount: number;
  lastDetectedAt: string;
}

export interface FailureClusterItem {
  clusterKey: string;
  representativeError: string;
  occurrenceCount: number;
  firstSeen: string;
  lastSeen: string;
}

export interface TriageResponse {
  id: number;
  testExecutionId: number;
  triageStatus: string;
  issueLink?: string;
  comment?: string;
}

export interface TriageSummary {
  totalTriaged: number;
  untriaged: number;
  breakdown: Record<string, number>;
}

export interface SummaryResponse {
  totalRuns: number;
  overallPassRate: number;
  totalFailures: number;
  topFailure?: {
    testName: string;
    errorMessage: string;
    failCount: number;
  };
  topFlakyTests: { testCaseKey: string; flakyScore: number }[];
}
