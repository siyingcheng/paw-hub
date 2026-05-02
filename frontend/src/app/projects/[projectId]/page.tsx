"use client"

import { useEffect, useState } from "react"
import { useParams } from "next/navigation"
import Navbar from "@/components/layout/Navbar"
import Sidebar from "@/components/layout/Sidebar"
import KpiCards from "@/components/dashboard/KpiCards"
import PassRateChart from "@/components/dashboard/PassRateChart"
import RecentRegressions from "@/components/dashboard/RecentRegressions"
import TriageBreakdown from "@/components/dashboard/TriageBreakdown"
import { api } from "@/lib/api"
import { toast } from "sonner"
import { TrendResponse, FlakyTest, TriageSummary } from "@/lib/types"
import { Skeleton } from "@/components/ui/skeleton"

export default function DashboardPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const id = Number(projectId)
  const [role, setRole] = useState<string>("")
  const [trends, setTrends] = useState<TrendResponse[]>([])
  const [flaky, setFlaky] = useState<FlakyTest[]>([])
  const [triageSummary, setTriageSummary] = useState<TriageSummary | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.auth
      .getRole(id)
      .then(r => setRole(r.role))
      .catch(() => {})
  }, [id])

  useEffect(() => {
    setLoading(true)
    Promise.all([
      api.analysis.getTrends(id),
      api.analysis.getFlakyTests(id),
      api.triage.getSummary(id),
    ])
      .then(([t, f, ts]) => {
        setTrends(t)
        setFlaky(f)
        setTriageSummary(ts)
      })
      .catch(err => {
        toast.error(
          "Failed to load dashboard: " +
            (err instanceof Error ? err.message : "Unknown error")
        )
      })
      .finally(() => setLoading(false))
  }, [id])

  return (
    <div>
      <Navbar projectId={id} />
      <div className="flex">
        <Sidebar projectId={id} role={role} />
        <main className="flex-1 p-6 space-y-6">
          {loading ? (
            <div className="space-y-6">
              <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                <Skeleton className="h-24 rounded-xl" />
                <Skeleton className="h-24 rounded-xl" />
                <Skeleton className="h-24 rounded-xl" />
                <Skeleton className="h-24 rounded-xl" />
              </div>
              <div className="grid grid-cols-2 gap-6">
                <Skeleton className="h-80 rounded-xl" />
                <Skeleton className="h-80 rounded-xl" />
              </div>
              <Skeleton className="h-48 rounded-xl" />
            </div>
          ) : (
            <>
              <KpiCards
                trends={trends}
                flakyCount={flaky.length}
                triageSummary={triageSummary}
              />
              <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                <PassRateChart trends={trends} />
                <RecentRegressions projectId={id} />
              </div>
              <TriageBreakdown summary={triageSummary} />
            </>
          )}
        </main>
      </div>
    </div>
  )
}
