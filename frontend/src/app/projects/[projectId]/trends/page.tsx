"use client"

import { useEffect, useState } from "react"
import { useParams } from "next/navigation"
import Navbar from "@/components/layout/Navbar"
import Sidebar from "@/components/layout/Sidebar"
import PassRateChart from "@/components/dashboard/PassRateChart"
import { api } from "@/lib/api"
import { toast } from "sonner"
import { TrendResponse, FlakyTest, FailureClusterItem } from "@/lib/types"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Skeleton } from "@/components/ui/skeleton"

export default function TrendsPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const id = Number(projectId)
  const [role, setRole] = useState<string>("")
  const [trends, setTrends] = useState<TrendResponse[]>([])
  const [flaky, setFlaky] = useState<FlakyTest[]>([])
  const [clusters, setClusters] = useState<FailureClusterItem[]>([])
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
      api.analysis.getClusters(id),
    ])
      .then(([t, f, c]) => {
        setTrends(t)
        setFlaky(f)
        setClusters(c)
      })
      .catch(err => {
        toast.error(
          "Failed to load trends: " +
            (err instanceof Error ? err.message : "Unknown error")
        )
      })
      .finally(() => setLoading(false))
  }, [id])

  if (loading) {
    return (
      <div>
        <Navbar projectId={id} />
        <div className="flex">
          <Sidebar projectId={id} role={role} />
          <main className="flex-1 p-6 space-y-6">
            <Skeleton className="h-80 rounded-xl" />
            <div className="grid grid-cols-2 gap-6">
              <Skeleton className="h-64 rounded-xl" />
              <Skeleton className="h-64 rounded-xl" />
            </div>
          </main>
        </div>
      </div>
    )
  }

  return (
    <div>
      <Navbar projectId={id} />
      <div className="flex">
        <Sidebar projectId={id} role={role} />
        <main className="flex-1 p-6 space-y-6">
          <PassRateChart trends={trends} />
          <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle className="text-sm text-muted-foreground">
                  FLAKY TESTS
                </CardTitle>
              </CardHeader>
              <CardContent>
                {flaky.length === 0 ? (
                  <p className="text-muted-foreground text-sm">
                    No flaky tests detected
                  </p>
                ) : (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Test</TableHead>
                        <TableHead className="text-right">Score</TableHead>
                        <TableHead className="text-right">
                          Transitions
                        </TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {flaky.map(f => (
                        <TableRow key={f.testCaseKey}>
                          <TableCell>{f.testCaseKey}</TableCell>
                          <TableCell className="text-right text-yellow-500">
                            {(f.flakyScore * 100).toFixed(0)}%
                          </TableCell>
                          <TableCell className="text-right text-muted-foreground">
                            {f.transitionCount}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-sm text-muted-foreground">
                  FAILURE CLUSTERS
                </CardTitle>
              </CardHeader>
              <CardContent>
                {clusters.length === 0 ? (
                  <p className="text-muted-foreground text-sm">No clusters</p>
                ) : (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Error</TableHead>
                        <TableHead className="text-right">Count</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {clusters.map(c => (
                        <TableRow key={c.clusterKey}>
                          <TableCell className="truncate max-w-[300px]">
                            {c.representativeError}
                          </TableCell>
                          <TableCell className="text-right text-muted-foreground">
                            {c.occurrenceCount}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
              </CardContent>
            </Card>
          </div>
        </main>
      </div>
    </div>
  )
}
