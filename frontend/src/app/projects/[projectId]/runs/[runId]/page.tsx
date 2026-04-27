'use client';
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Navbar from "@/components/layout/Navbar";
import Sidebar from "@/components/layout/Sidebar";
import FilterBar from "@/components/explorer/FilterBar";
import TestTable from "@/components/explorer/TestTable";
import { api } from "@/lib/api";
import { TestExecution, TestRun } from "@/lib/types";

export default function TestExplorerPage() {
  const { projectId, runId } = useParams<{ projectId: string; runId: string }>();
  const id = Number(projectId);
  const [run, setRun] = useState<TestRun | null>(null);
  const [executions, setExecutions] = useState<TestExecution[]>([]);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [envFilter, setEnvFilter] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    api.collection.getRun(id, Number(runId)).then((data: any) => {
      setRun(data);
      setExecutions(data.executions || []);
    }).catch(err => console.error('Test explorer load error:', err));
  }, [id, runId, refreshKey]);

  const filtered = executions.filter(e => {
    if (search && !e.testName.toLowerCase().includes(search.toLowerCase()) && !e.className.toLowerCase().includes(search.toLowerCase())) return false;
    if (statusFilter && e.status !== statusFilter) return false;
    return true;
  });

  return (
    <div>
      <Navbar projectName={`Run #${runId}`} />
      <div className="flex">
        <Sidebar projectId={id} />
        <main className="flex-1 p-6">
          {run && (
            <div className="flex gap-3 mb-4 text-sm">
              <span className="text-green-400">Passed: {run.passed}</span>
              <span className="text-red-400">Failed: {run.failed}</span>
              <span className="text-yellow-400">Skipped: {run.skipped}</span>
              <span className="text-gray-400">Duration: {run.durationMs}ms</span>
              <span className="text-gray-500">Env: {run.environment}</span>
            </div>
          )}
          <FilterBar search={search} onSearchChange={setSearch} statusFilter={statusFilter} onStatusChange={setStatusFilter} envFilter={envFilter} onEnvChange={setEnvFilter} />
          <TestTable projectId={id} executions={filtered} onTriageSaved={() => setRefreshKey(k => k + 1)} />
        </main>
      </div>
    </div>
  );
}
