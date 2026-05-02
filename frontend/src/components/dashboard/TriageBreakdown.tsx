import { TriageSummary } from "@/lib/types"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

const CATEGORY_LABELS: Record<string, string> = {
  NEW_BUG: "New Bug",
  KNOWN_ISSUE: "Known Issue",
  SCRIPT_ISSUE: "Script Issue",
  DATA_ISSUE: "Data Issue",
  ENV_ISSUE: "Env Issue",
  CR: "Code Review",
  OTHER: "Other",
  UNTRIAGED: "Untriaged",
}

export default function TriageBreakdown({
  summary,
}: {
  summary: TriageSummary | null
}) {
  if (!summary) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="text-sm text-muted-foreground">
            TRIAGE BREAKDOWN
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground text-sm">No data</p>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-sm text-muted-foreground">
          FAILURES BY CATEGORY (30d)
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
          {Object.entries(CATEGORY_LABELS).map(([key, label]) => (
            <div
              key={key}
              className="flex justify-between text-sm bg-secondary rounded-lg px-3 py-2"
            >
              <span className="text-muted-foreground">{label}</span>
              <span className="font-semibold">
                {summary.breakdown[key] || 0}
              </span>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
