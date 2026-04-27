'use client';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { TrendResponse } from "@/lib/types";

interface Props { trends: TrendResponse[] }
const COLORS: Record<string, string> = { dev: '#3b82f6', staging: '#f59e0b', prod: '#22c55e' };

export default function PassRateChart({ trends }: Props) {
  if (!trends.length) return <div className="bg-gray-900 rounded-xl border border-gray-800 p-4">No trend data</div>;

  const dateMap = new Map<string, Record<string, number>>();
  trends.forEach(t => {
    t.dataPoints.forEach(dp => {
      const rec = dateMap.get(dp.date) || {};
      rec[t.environment] = dp.passRate * 100;
      dateMap.set(dp.date, rec);
    });
  });
  const data = Array.from(dateMap.entries()).map(([date, envs]) => ({ date, ...envs }));

  return (
    <div className="bg-gray-900 rounded-xl border border-gray-800 p-4">
      <h3 className="text-sm text-gray-400 mb-4">PASS RATE TREND</h3>
      <ResponsiveContainer width="100%" height={250}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
          <XAxis dataKey="date" stroke="#94a3b8" fontSize={12} />
          <YAxis stroke="#94a3b8" fontSize={12} domain={[80, 100]} />
          <Tooltip />
          <Legend />
          {['dev','staging','prod'].map(env =>
            <Line key={env} type="monotone" dataKey={env} stroke={COLORS[env]} strokeWidth={2} dot={false} connectNulls />
          )}
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
