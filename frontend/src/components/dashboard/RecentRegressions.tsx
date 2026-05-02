"use client"

import { useEffect, useState } from "react"
import { api } from "@/lib/api"
import { RegressionResponse } from "@/lib/types"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { AlertTriangle, Diamond } from "lucide-react"

interface Props {
  projectId: number
}

export default function RecentRegressions({ projectId }: Props) {
  const [regressions, setRegressions] = useState<RegressionResponse[]>([])

  useEffect(() => {
    api.analysis.getRegressions(projectId).then(r => setRegressions(r))
  }, [projectId])

  const allCases = regressions.flatMap(r => r.regressedCases)
  const hasData =
    regressions.some(r => r.runLevelRegression) || allCases.length > 0

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-sm text-muted-foreground">
          RECENT REGRESSIONS
        </CardTitle>
      </CardHeader>
      <CardContent>
        {!hasData ? (
          <p className="text-muted-foreground text-sm">
            No regressions detected
          </p>
        ) : (
          <div className="space-y-3">
            {regressions
              .filter(r => r.runLevelRegression)
              .map((r, i) => (
                <Alert key={`run-${i}`} variant="destructive">
                  <AlertTriangle className="h-4 w-4" />
                  <AlertDescription>
                    <div className="font-medium text-sm">
                      Run-level regression
                    </div>
                    {r.runLevelDetail && (
                      <div className="text-xs opacity-80 mt-0.5">
                        {r.runLevelDetail}
                      </div>
                    )}
                  </AlertDescription>
                </Alert>
              ))}
            {allCases.slice(0, 5).map((c, i) => (
              <div
                key={`case-${i}`}
                className="flex items-center gap-2 text-sm"
              >
                <Diamond className="h-3 w-3 text-red-500" />
                <span className="truncate">{c}</span>
                <Badge variant="destructive" className="ml-auto text-xs">
                  regressed
                </Badge>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
