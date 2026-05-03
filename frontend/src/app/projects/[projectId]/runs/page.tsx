"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { useParams } from "next/navigation"
import Navbar from "@/components/layout/Navbar"
import Sidebar from "@/components/layout/Sidebar"
import { api } from "@/lib/api"
import { TestRun } from "@/lib/types"
import { toast } from "sonner"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"
import { ChevronRight } from "lucide-react"

export default function RunsListPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const id = Number(projectId)
  const [role, setRole] = useState<string>("")
  const [runs, setRuns] = useState<TestRun[]>([])
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
      .getRuns(id)
      .then(res => setRuns(res.items))
      .catch(err => toast.error(err.message))
      .finally(() => setLoading(false))
  }, [id])

  return (
    <div>
      <Navbar projectId={id} />
      <div className="flex">
        <Sidebar projectId={id} role={role} />
        <main className="flex-1 p-6">
          <Card>
            <CardHeader>
              <CardTitle>Test Runs</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <div className="space-y-2">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <Skeleton key={i} className="h-10 w-full" />
                  ))}
                </div>
              ) : runs.length === 0 ? (
                <p className="text-muted-foreground text-sm py-4 text-center">
                  No test runs yet
                </p>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Run</TableHead>
                      <TableHead>Env</TableHead>
                      <TableHead>Branch</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Passed/Failed</TableHead>
                      <TableHead className="w-12" />
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {runs.map(run => (
                      <TableRow key={run.id}>
                        <TableCell className="font-mono text-xs">
                          {run.runIdentifier || `#${run.id}`}
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline" className="text-xs">
                            {run.environment}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-muted-foreground text-xs">
                          {run.branch || "—"}
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant={
                              run.status === "PASS" ? "default" : "destructive"
                            }
                            className="text-xs"
                          >
                            {run.status}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-xs">
                          <span className="text-green-500">{run.passed}</span>
                          <span className="text-muted-foreground"> / </span>
                          <span className="text-red-500">{run.failed}</span>
                        </TableCell>
                        <TableCell>
                          <Link href={`/projects/${id}/runs/${run.id}`}>
                            <ChevronRight className="h-4 w-4 text-muted-foreground" />
                          </Link>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </main>
      </div>
    </div>
  )
}
