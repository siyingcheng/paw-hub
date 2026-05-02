"use client"

import { useEffect, useState } from "react"
import { TestExecution, TriageResponse } from "@/lib/types"
import { api } from "@/lib/api"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import ErrorDetail from "./ErrorDetail"
import TriageModal from "./TriageModal"

interface Props {
  projectId: number
  executions: TestExecution[]
  onTriageSaved: () => void
}

function statusVariant(
  status: string
): "default" | "secondary" | "destructive" | "outline" {
  switch (status) {
    case "PASS":
      return "default"
    case "FAIL":
      return "destructive"
    case "SKIP":
      return "secondary"
    case "ERROR":
      return "destructive"
    default:
      return "outline"
  }
}

export default function TestTable({
  projectId,
  executions,
  onTriageSaved,
}: Props) {
  const [triages, setTriages] = useState<Map<number, TriageResponse>>(new Map())

  useEffect(() => {
    Promise.all(executions.map(e => api.triage.get(projectId, e.id))).then(
      tList => {
        const map = new Map<number, TriageResponse>()
        tList.forEach((t, i) => {
          if (t) map.set(executions[i].id, t)
        })
        setTriages(map)
      }
    )
  }, [executions, projectId])

  const handleTriageSaved = () => {
    onTriageSaved()
    Promise.all(executions.map(e => api.triage.get(projectId, e.id))).then(
      tList => {
        const map = new Map<number, TriageResponse>()
        tList.forEach((t, i) => {
          if (t) map.set(executions[i].id, t)
        })
        setTriages(map)
      }
    )
  }

  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-10">#</TableHead>
            <TableHead>Case ID</TableHead>
            <TableHead>Suite / Class</TableHead>
            <TableHead>Test</TableHead>
            <TableHead className="w-12">Att</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Duration</TableHead>
            <TableHead>Error</TableHead>
            <TableHead>Triage</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {executions.map((e, i) => {
            const t = triages.get(e.id)
            const isFailure = e.status === "FAIL" || e.status === "ERROR"
            return (
              <TableRow
                key={e.id}
                className={isFailure ? "bg-destructive/5" : undefined}
              >
                <TableCell className="text-muted-foreground">
                  {i + 1}
                </TableCell>
                <TableCell className="text-yellow-500 font-mono text-xs">
                  {e.caseNumber || "—"}
                </TableCell>
                <TableCell className="text-muted-foreground text-xs">
                  {e.className}
                </TableCell>
                <TableCell>{e.testName}</TableCell>
                <TableCell className="text-muted-foreground">
                  {e.attempt}
                </TableCell>
                <TableCell>
                  <Badge variant={statusVariant(e.status)} className="text-xs">
                    {e.status}
                  </Badge>
                </TableCell>
                <TableCell className="text-muted-foreground">
                  {e.durationMs}ms
                </TableCell>
                <TableCell>
                  <ErrorDetail
                    errorMessage={e.errorMessage}
                    stackTrace={e.stackTrace}
                    errorType={e.errorType}
                  />
                </TableCell>
                <TableCell>
                  {isFailure ? (
                    <TriageModal
                      projectId={projectId}
                      executionId={e.id}
                      currentStatus={
                        t?.triageStatus || "UNTRIAGED"
                      }
                      currentIssueLink={t?.issueLink || ""}
                      currentComment={t?.comment || ""}
                      onSaved={handleTriageSaved}
                    />
                  ) : null}
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}
