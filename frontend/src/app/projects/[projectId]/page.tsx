'use client';
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Navbar from "@/components/layout/Navbar";
import Sidebar from "@/components/layout/Sidebar";
import KpiCards from "@/components/dashboard/KpiCards";
import PassRateChart from "@/components/dashboard/PassRateChart";
import RecentRegressions from "@/components/dashboard/RecentRegressions";
import TriageBreakdown from "@/components/dashboard/TriageBreakdown";
import { api } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { TrendResponse, FlakyTest, TriageSummary } from "@/lib/types";

export default function DashboardPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const id = Number(projectId);
  const toast = useToast();
  const [trends, setTrends] = useState<TrendResponse[]>([]);
  const [flaky, setFlaky] = useState<FlakyTest[]>([]);
  const [triageSummary, setTriageSummary] = useState<TriageSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    Promise.all([
      api.analysis.getTrends(id),
      api.analysis.getFlakyTests(id),
      api.triage.getSummary(id),
    ]).then(([t, f, ts]) => {
      setTrends(t);
      setFlaky(f);
      setTriageSummary(ts);
    }).catch(err => {
      toast.error("Failed to load dashboard: " + (err instanceof Error ? err.message : "Unknown error"));
    }).finally(() => setLoading(false));
  }, [id]);

  return (
    <div>
      <Navbar projectName="Project" />
      <div className="flex">
        <Sidebar projectId={id} />
        <main className="flex-1 p-6 space-y-6">
          {loading ? (
            <div className="flex items-center justify-center h-64 text-gray-400">Loading dashboard...</div>
          ) : (
            <>
              <KpiCards trends={trends} flakyCount={flaky.length} triageSummary={triageSummary} />
              <div className="grid grid-cols-2 gap-6">
                <PassRateChart trends={trends} />
                <RecentRegressions projectId={id} />
              </div>
              <TriageBreakdown summary={triageSummary} />
            </>
          )}
        </main>
      </div>
    </div>
  );
}
