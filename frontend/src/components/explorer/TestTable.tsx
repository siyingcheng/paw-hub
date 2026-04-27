'use client';
import { useEffect, useState } from "react";
import { TestExecution, TriageResponse } from "@/lib/types";
import { api } from "@/lib/api";
import ErrorDetail from "./ErrorDetail";
import TriageModal from "./TriageModal";

interface Props {
  projectId: number;
  executions: TestExecution[];
  onTriageSaved: () => void;
}

export default function TestTable({ projectId, executions, onTriageSaved }: Props) {
  const [triages, setTriages] = useState<Map<number, TriageResponse>>(new Map());

  useEffect(() => {
    Promise.all(executions.map(e => api.triage.get(projectId, e.id))).then(tList => {
      const map = new Map<number, TriageResponse>();
      tList.forEach((t, i) => { if (t) map.set(executions[i].id, t); });
      setTriages(map);
    });
  }, [executions, projectId]);

  const handleTriageSaved = () => {
    onTriageSaved();
    // Refresh triages
    Promise.all(executions.map(e => api.triage.get(projectId, e.id))).then(tList => {
      const map = new Map<number, TriageResponse>();
      tList.forEach((t, i) => { if (t) map.set(executions[i].id, t); });
      setTriages(map);
    });
  };

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr className="border-b border-gray-800 text-left text-gray-400 text-xs">
            <th className="py-2 px-3">#</th>
            <th className="py-2 px-3">Case ID</th>
            <th className="py-2 px-3">Suite / Class</th>
            <th className="py-2 px-3">Test</th>
            <th className="py-2 px-3">Att</th>
            <th className="py-2 px-3">Status</th>
            <th className="py-2 px-3">Duration</th>
            <th className="py-2 px-3">Error</th>
            <th className="py-2 px-3">Triage</th>
          </tr>
        </thead>
        <tbody>
          {executions.map((e, i) => {
            const t = triages.get(e.id);
            return (
              <tr key={e.id} className={`border-b border-gray-800/50 ${e.status === 'FAIL' || e.status === 'ERROR' ? 'bg-red-950/20' : ''}`}>
                <td className="py-2 px-3 text-gray-500">{i + 1}</td>
                <td className="py-2 px-3 text-yellow-500">{e.caseNumber || '—'}</td>
                <td className="py-2 px-3 text-gray-400">{e.className}</td>
                <td className="py-2 px-3">{e.testName}</td>
                <td className="py-2 px-3 text-gray-500">{e.attempt}</td>
                <td className="py-2 px-3">
                  <span className={`text-xs px-2 py-0.5 rounded ${e.status === 'PASS' ? 'bg-green-900 text-green-400' : e.status === 'FAIL' ? 'bg-red-900 text-red-400' : e.status === 'SKIP' ? 'bg-yellow-900 text-yellow-400' : 'bg-red-900 text-red-400'}`}>{e.status}</span>
                </td>
                <td className="py-2 px-3 text-gray-400">{e.durationMs}ms</td>
                <td className="py-2 px-3"><ErrorDetail errorMessage={e.errorMessage} stackTrace={e.stackTrace} errorType={e.errorType} /></td>
                <td className="py-2 px-3">
                  {(e.status === 'FAIL' || e.status === 'ERROR') ? (
                    <TriageModal
                      projectId={projectId}
                      executionId={e.id}
                      currentStatus={t?.triageStatus || 'UNTRIAGED'}
                      currentIssueLink={t?.issueLink || ''}
                      currentComment={t?.comment || ''}
                      onSaved={handleTriageSaved}
                    />
                  ) : null}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
