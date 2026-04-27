'use client';
import { useEffect, useState } from "react";
import { api } from "@/lib/api";

interface Props { projectId: number }

export default function RecentRegressions({ projectId }: Props) {
  const [flaky, setFlaky] = useState<{ testCaseKey: string; flakyScore: number }[]>([]);

  useEffect(() => {
    api.analysis.getFlakyTests(projectId).then(f => setFlaky(f.slice(0, 5)));
  }, [projectId]);

  return (
    <div className="bg-gray-900 rounded-xl border border-gray-800 p-4">
      <h3 className="text-sm text-gray-400 mb-4">TOP FLAKY TESTS</h3>
      {flaky.length === 0 ? <p className="text-gray-500 text-sm">None detected</p> : (
        <div className="space-y-2">
          {flaky.map(f => (
            <div key={f.testCaseKey} className="flex justify-between text-sm">
              <span className="text-gray-300 truncate">{f.testCaseKey}</span>
              <span className="text-yellow-400 ml-2">{(f.flakyScore * 100).toFixed(0)}%</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
