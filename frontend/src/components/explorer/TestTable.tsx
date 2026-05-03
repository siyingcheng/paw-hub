"use client"

import { useState } from "react"
import { TestExecution } from "@/lib/types"
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
  const [refreshKey, setRefreshKey] = useState(0)

  const handleTriageSaved = () => {
    onTriageSaved()
    setRefreshKey(k => k + 1)
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
                      currentStatus={e.triageStatus || "UNTRIAGED"}
                      currentIssueLink={e.issueLink || ""}
                      currentComment=""
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
