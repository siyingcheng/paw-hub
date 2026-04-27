'use client';
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Navbar from "@/components/layout/Navbar";
import Sidebar from "@/components/layout/Sidebar";
import FilterBar from "@/components/explorer/FilterBar";
import TestTable from "@/components/explorer/TestTable";
import { api } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { TestExecution, TestRun } from "@/lib/types";

export default function TestExplorerPage() {
  const { projectId, runId } = useParams<{ projectId: string; runId: string }>();
  const id = Number(projectId);
  const toast = useToast();
  const [role, setRole] = useState<string>("");
  const [run, setRun] = useState<TestRun | null>(null);
  const [executions, setExecutions] = useState<TestExecution[]>([]);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [envFilter, setEnvFilter] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.auth.getRole(id).then(r => setRole(r.role)).catch(() => {});
  }, [id]);

  useEffect(() => {
    setLoading(true);
    api.collection.getRun(id, Number(runId)).then((data: any) => {
      setRun(data);
      setExecutions(data.executions || []);
    }).catch(err => {
      toast.error("Failed to load test run: " + (err instanceof Error ? err.message : "Not found"));
    }).finally(() => setLoading(false));
  }, [id, runId, refreshKey]);

  const filtered = executions.filter(e => {
    if (search && !e.testName.toLowerCase().includes(search.toLowerCase()) && !e.className.toLowerCase().includes(search.toLowerCase())) return false;
    if (statusFilter && e.status !== statusFilter) return false;
    return true;
  });

  if (loading) {
    return (
      <div>
        <Navbar projectName={`Run #${runId}`} />
        <div className="flex">
          <Sidebar projectId={id} role={role} />
          <main className="flex-1 p-6">
            <div className="flex items-center justify-center h-64 text-gray-400">Loading test run...</div>
          </main>
        </div>
      </div>
    );
  }

  return (
    <div>
      <Navbar projectName={`Run #${runId}`} />
      <div className="flex">
        <Sidebar projectId={id} role={role} />
        <main className="flex-1 p-6">
          {run && (
            <div className="flex gap-3 mb-4 text-sm">
              <span className="text-green-400">Passed: {run.passed}</span>
              <span className="text-red-400">Failed: {run.failed}</span>
              <span className="text-yellow-400">Skipped: {run.skipped}</span>
              <span className="text-gray-400">Duration: {run.durationMs}ms</span>
              <span className="text-gray-500">Env: {run.environment}</span>
              <span className="text-gray-500">Branch: {run.branch || '—'}</span>
            </div>
          )}
          <FilterBar search={search} onSearchChange={setSearch} statusFilter={statusFilter} onStatusChange={setStatusFilter} envFilter={envFilter} onEnvChange={setEnvFilter} />
          <TestTable projectId={id} executions={filtered} onTriageSaved={() => { setRefreshKey(k => k + 1); toast.success("Triage saved"); }} />
        </main>
      </div>
    </div>
  );
}
