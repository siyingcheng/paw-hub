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
import { TrendResponse, FlakyTest, TriageSummary } from "@/lib/types";

export default function DashboardPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const id = Number(projectId);
  const [trends, setTrends] = useState<TrendResponse[]>([]);
  const [flaky, setFlaky] = useState<FlakyTest[]>([]);
  const [triageSummary, setTriageSummary] = useState<TriageSummary | null>(null);

  useEffect(() => {
    Promise.all([
      api.analysis.getTrends(id),
      api.analysis.getFlakyTests(id),
      api.triage.getSummary(id),
    ]).then(([t, f, ts]) => {
      setTrends(t);
      setFlaky(f);
      setTriageSummary(ts);
    });
  }, [id]);

  return (
    <div>
      <Navbar projectName="Project" />
      <div className="flex">
        <Sidebar projectId={id} />
        <main className="flex-1 p-6 space-y-6">
          <KpiCards trends={trends} flakyCount={flaky.length} triageSummary={triageSummary} />
          <div className="grid grid-cols-2 gap-6">
            <PassRateChart trends={trends} />
            <RecentRegressions projectId={id} />
          </div>
          <TriageBreakdown summary={triageSummary} />
        </main>
      </div>
    </div>
  );
}
