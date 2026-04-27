'use client';
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Navbar from "@/components/layout/Navbar";
import Sidebar from "@/components/layout/Sidebar";
import PassRateChart from "@/components/dashboard/PassRateChart";
import { api } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { TrendResponse, FlakyTest, FailureClusterItem } from "@/lib/types";

export default function TrendsPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const id = Number(projectId);
  const toast = useToast();
  const [role, setRole] = useState<string>("");
  const [trends, setTrends] = useState<TrendResponse[]>([]);
  const [flaky, setFlaky] = useState<FlakyTest[]>([]);
  const [clusters, setClusters] = useState<FailureClusterItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.auth.getRole(id).then(r => setRole(r.role)).catch(() => {});
  }, [id]);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      api.analysis.getTrends(id),
      api.analysis.getFlakyTests(id),
      api.analysis.getClusters(id),
    ]).then(([t, f, c]) => {
      setTrends(t); setFlaky(f); setClusters(c);
    }).catch(err => {
      toast.error("Failed to load trends: " + (err instanceof Error ? err.message : "Unknown error"));
    }).finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div>
        <Navbar projectName="Trends" />
        <div className="flex">
          <Sidebar projectId={id} role={role} />
          <main className="flex-1 p-6">
            <div className="flex items-center justify-center h-64 text-gray-400">Loading trends...</div>
          </main>
        </div>
      </div>
    );
  }

  return (
    <div>
      <Navbar projectName="Trends" />
      <div className="flex">
        <Sidebar projectId={id} role={role} />
        <main className="flex-1 p-6 space-y-6">
          <PassRateChart trends={trends} />
          <div className="grid grid-cols-2 gap-6">
            <div className="bg-gray-900 rounded-xl border border-gray-800 p-4">
              <h3 className="text-sm text-gray-400 mb-4">FLAKY TESTS</h3>
              {flaky.length === 0 ? <p className="text-gray-500 text-sm">No flaky tests detected</p> : (
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-left text-gray-500 text-xs"><th className="py-2">Test</th><th className="py-2 text-right">Score</th><th className="py-2 text-right">Transitions</th></tr>
                  </thead>
                  <tbody>
                    {flaky.map(f => (
                      <tr key={f.testCaseKey} className="border-t border-gray-800">
                        <td className="py-2 text-gray-300">{f.testCaseKey}</td>
                        <td className="py-2 text-right text-yellow-400">{(f.flakyScore * 100).toFixed(0)}%</td>
                        <td className="py-2 text-right text-gray-400">{f.transitionCount}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
            <div className="bg-gray-900 rounded-xl border border-gray-800 p-4">
              <h3 className="text-sm text-gray-400 mb-4">FAILURE CLUSTERS</h3>
              {clusters.length === 0 ? <p className="text-gray-500 text-sm">No clusters</p> : (
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-left text-gray-500 text-xs"><th className="py-2">Error</th><th className="py-2 text-right">Count</th></tr>
                  </thead>
                  <tbody>
                    {clusters.map(c => (
                      <tr key={c.clusterKey} className="border-t border-gray-800">
                        <td className="py-2 text-gray-300 truncate max-w-[300px]">{c.representativeError}</td>
                        <td className="py-2 text-right text-gray-400">{c.occurrenceCount}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
