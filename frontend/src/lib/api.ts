import { getToken, clearAuth } from './auth';
import type {
  TestRun, TestExecution, TrendResponse, FlakyTest,
  FailureClusterItem, TriageResponse, TriageSummary, SummaryResponse,
  MeResponse, RegressionResponse, TeamMember, ProjectInfo,
} from './types';

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    ...(options?.headers as Record<string, string> || {}),
  };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  if (!(options?.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  const res = await fetch(`${BASE_URL}/api/v1${path}`, { ...options, headers });

  let json: any;
  try { json = await res.json(); } catch { json = {} }

  if (json.success === undefined && (res.status === 401 || res.status === 403)) {
    clearAuth();
    if (typeof window !== 'undefined') window.location.href = '/login?expired=true';
    throw new Error('Session expired. Please log in again.');
  }

  if (!json.success) throw new Error(json.message || 'Request failed');
  return json.data;
}

export const api = {
  project: {
    get: (projectId: number) =>
      request<ProjectInfo>(`/projects/${projectId}`),
  },

  auth: {
    login: (username: string, password: string) =>
      request<{token: string; userId: number; username: string}>('/auth/login', {
        method: 'POST', body: JSON.stringify({ username, password }),
      }),
    register: (username: string, email: string, password: string) =>
      request<{token: string; userId: number; username: string}>('/auth/register', {
        method: 'POST', body: JSON.stringify({ username, email, password }),
      }),
    me: () => request<MeResponse>('/auth/me'),
    getRole: (projectId: number) =>
      request<{ role: string }>(`/projects/${projectId}/my-role`),
  },

  collection: {
    uploadXml: (projectId: number, formData: FormData) =>
      request<TestRun>(`/projects/${projectId}/test-results`, {
        method: 'POST', body: formData,
      }),
    getRuns: (projectId: number, params?: string) =>
      request<TestRun[]>(`/projects/${projectId}/test-runs${params ? '?' + params : ''}`),
    getRun: (projectId: number, runId: number) =>
      request<TestRun & { executions: TestExecution[] }>(`/projects/${projectId}/test-runs/${runId}`),
  },

  analysis: {
    getTrends: (projectId: number, period = 'daily', days = 30, env?: string) =>
      request<TrendResponse[]>(`/projects/${projectId}/trends?period=${period}&days=${days}${env ? '&environment=' + env : ''}`),
    getFlakyTests: (projectId: number) =>
      request<FlakyTest[]>(`/projects/${projectId}/flaky-tests`),
    getClusters: (projectId: number) =>
      request<FailureClusterItem[]>(`/projects/${projectId}/failure-clusters`),
    getRegressions: (projectId: number) =>
      request<RegressionResponse[]>(`/projects/${projectId}/regressions`),
  },

  triage: {
    save: (projectId: number, executionId: number, data: { triageStatus: string; issueLink?: string; comment?: string }) =>
      request<TriageResponse>(`/projects/${projectId}/test-executions/${executionId}/triage`, {
        method: 'PUT', body: JSON.stringify(data),
      }),
    get: (projectId: number, executionId: number) =>
      request<TriageResponse>(`/projects/${projectId}/test-executions/${executionId}/triage`),
    getSummary: (projectId: number) =>
      request<TriageSummary>(`/projects/${projectId}/triage-summary`),
  },

  reporting: {
    getSummary: (projectId: number, days = 30) =>
      request<SummaryResponse>(`/projects/${projectId}/summary?days=${days}`),
    export: (projectId: number, format = 'json', days = 30) =>
      request<string>(`/projects/${projectId}/export?format=${format}&days=${days}`),
  },

  team: {
    listMembers: (projectId: number) =>
      request<TeamMember[]>(`/projects/${projectId}/team/members`),
    upsertRole: (projectId: number, userId: number, role: string) =>
      request<TeamMember>(`/projects/${projectId}/team/members/${userId}`, {
        method: 'PUT', body: JSON.stringify({ role }),
      }),
    removeMember: (projectId: number, userId: number) =>
      request<void>(`/projects/${projectId}/team/members/${userId}`, {
        method: 'DELETE',
      }),
  },
};
