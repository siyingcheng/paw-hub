import { TrendResponse, TriageSummary } from "@/lib/types"
import { Card, CardContent } from "@/components/ui/card"

interface Props {
  trends: TrendResponse[]
  flakyCount: number
  triageSummary: TriageSummary | null
}

export default function KpiCards({ trends, flakyCount, triageSummary }: Props) {
  const prodTrend = trends.find(t => t.environment === "prod")
  const lastPoint = prodTrend?.dataPoints?.slice(-1)[0]

  const items = [
    {
      label: "PASS RATE (LATEST)",
      value: lastPoint ? (lastPoint.passRate * 100).toFixed(1) + "%" : "—",
      color: "text-green-500",
    },
    {
      label: "FLAKY TESTS",
      value: String(flakyCount),
      color: "text-yellow-500",
    },
    {
      label: "UNTRIAGED",
      value: triageSummary?.untriaged ?? "—",
      color: "text-red-500",
    },
    {
      label: "TOTAL TRIAGED (30d)",
      value: triageSummary?.totalTriaged ?? "—",
      color: "text-blue-500",
    },
  ]

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      {items.map(item => (
        <Card key={item.label}>
          <CardContent className="p-4">
            <div className="text-xs text-muted-foreground">{item.label}</div>
            <div className={`text-2xl font-bold mt-1 ${item.color}`}>
              {item.value}
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
