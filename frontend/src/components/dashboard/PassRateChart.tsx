"use client"

import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts"
import { TrendResponse } from "@/lib/types"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

interface Props {
  trends: TrendResponse[]
}

const COLORS: Record<string, string> = {
  dev: "#3b82f6",
  staging: "#f59e0b",
  prod: "#22c55e",
}

export default function PassRateChart({ trends }: Props) {
  if (!trends.length) {
    return (
      <Card>
        <CardContent className="p-4 text-muted-foreground">
          No trend data
        </CardContent>
      </Card>
    )
  }

  const dateMap = new Map<string, Record<string, number>>()
  trends.forEach(t => {
    t.dataPoints.forEach(dp => {
      const rec = dateMap.get(dp.date) || {}
      rec[t.environment] = dp.passRate * 100
      dateMap.set(dp.date, rec)
    })
  })
  const data = Array.from(dateMap.entries()).map(([date, envs]) => ({
    date,
    ...envs,
  }))

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-sm text-muted-foreground">
          PASS RATE TREND
        </CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={250}>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
            <XAxis dataKey="date" stroke="#94a3b8" fontSize={12} />
            <YAxis stroke="#94a3b8" fontSize={12} domain={[80, 100]} />
            <Tooltip />
            <Legend />
            {["dev", "staging", "prod"].map(env => (
              <Line
                key={env}
                type="monotone"
                dataKey={env}
                stroke={COLORS[env]}
                strokeWidth={2}
                dot={false}
                connectNulls
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
}
