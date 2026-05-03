"use client"

import { useEffect, useState } from "react"
import { useParams } from "next/navigation"
import Navbar from "@/components/layout/Navbar"
import Sidebar from "@/components/layout/Sidebar"
import FilterBar from "@/components/explorer/FilterBar"
import TestTable from "@/components/explorer/TestTable"
import { api } from "@/lib/api"
import { toast } from "sonner"
import { TestExecution, TestRun } from "@/lib/types"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"

export default function TestExplorerPage() {
  const { projectId, runId } = useParams<{
    projectId: string
    runId: string
  }>()
  const id = Number(projectId)
  const [role, setRole] = useState<string>("")
  const [run, setRun] = useState<TestRun | null>(null)
  const [executions, setExecutions] = useState<TestExecution[]>([])
  const [search, setSearch] = useState("")
  const [statusFilter, setStatusFilter] = useState("")
  const [refreshKey, setRefreshKey] = useState(0)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.auth
      .getRole(id)
      .then(r => setRole(r.role))
      .catch(() => {})
  }, [id])

  useEffect(() => {
    setLoading(true)
    api.collection
      .getRun(id, Number(runId))
      .then((data: any) => {
        setRun(data)
        setExecutions(data.executions || [])
      })
      .catch(err => {
        toast.error(
          "Failed to load test run: " +
            (err instanceof Error ? err.message : "Not found")
        )
      })
      .finally(() => setLoading(false))
  }, [id, runId, refreshKey])

  const filtered = executions.filter(e => {
    if (
      search &&
      !e.testName.toLowerCase().includes(search.toLowerCase()) &&
      !e.className.toLowerCase().includes(search.toLowerCase())
    )
      return false
    if (statusFilter && statusFilter !== "all" && e.status !== statusFilter)
      return false
    return true
  })

  if (loading) {
    return (
      <div>
        <Navbar projectId={id} />
        <div className="flex">
          <Sidebar projectId={id} role={role} />
          <main className="flex-1 p-6 space-y-4">
            <div className="flex gap-3">
              <Skeleton className="h-6 w-24" />
              <Skeleton className="h-6 w-24" />
              <Skeleton className="h-6 w-24" />
              <Skeleton className="h-6 w-16" />
            </div>
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-96 rounded-md" />
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
        <main className="flex-1 p-6">
          {run && (
            <div className="flex gap-3 mb-4 text-sm flex-wrap items-center">
              <Badge
                variant="default"
                className="bg-green-600 hover:bg-green-600"
              >
                Passed: {run.passed}
              </Badge>
              <Badge variant="destructive">Failed: {run.failed}</Badge>
              <Badge variant="secondary">Skipped: {run.skipped}</Badge>
              <span className="text-muted-foreground">
                Duration: {run.durationMs}ms
              </span>
              <span className="text-muted-foreground">
                Env: {run.environment}
              </span>
              <span className="text-muted-foreground">
                Branch: {run.branch || "—"}
              </span>
            </div>
          )}
          <FilterBar
            search={search}
            onSearchChange={setSearch}
            statusFilter={statusFilter}
            onStatusChange={setStatusFilter}
          />
          <TestTable
            projectId={id}
            executions={filtered}
            onTriageSaved={() => {
              setRefreshKey(k => k + 1)
              toast.success("Triage saved")
            }}
          />
        </main>
      </div>
    </div>
  )
}
