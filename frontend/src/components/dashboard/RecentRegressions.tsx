'use client';
import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { RegressionResponse } from "@/lib/types";

interface Props { projectId: number }

export default function RecentRegressions({ projectId }: Props) {
  const [regressions, setRegressions] = useState<RegressionResponse[]>([]);

  useEffect(() => {
    api.analysis.getRegressions(projectId).then(r => setRegressions(r));
  }, [projectId]);

  const allCases = regressions.flatMap(r => r.regressedCases);
  const hasData = regressions.some(r => r.runLevelRegression) || allCases.length > 0;

  return (
    <div className="bg-gray-900 rounded-xl border border-gray-800 p-4">
      <h3 className="text-sm text-gray-400 mb-4">RECENT REGRESSIONS</h3>
      {!hasData ? (
        <p className="text-gray-500 text-sm">No regressions detected</p>
      ) : (
        <div className="space-y-3">
          {regressions.filter(r => r.runLevelRegression).map((r, i) => (
            <div key={`run-${i}`} className="flex items-start gap-2 p-2 bg-red-950/50 rounded-lg">
              <span className="text-red-400 mt-0.5">⚠</span>
              <div>
                <div className="text-red-300 text-sm font-medium">Run-level regression</div>
                {r.runLevelDetail && <div className="text-red-400/70 text-xs">{r.runLevelDetail}</div>}
              </div>
            </div>
          ))}
          {allCases.slice(0, 5).map((c, i) => (
            <div key={`case-${i}`} className="flex items-center gap-2">
              <span className="text-red-400 text-xs">◆</span>
              <span className="text-gray-300 text-sm truncate">{c}</span>
              <span className="text-red-400 text-xs ml-auto">regressed</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
