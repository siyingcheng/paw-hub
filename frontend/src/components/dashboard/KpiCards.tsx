import { TrendResponse, TriageSummary } from "@/lib/types";
import Card from "@/components/ui/Card";

interface Props { trends: TrendResponse[]; flakyCount: number; triageSummary: TriageSummary | null; }

export default function KpiCards({ trends, flakyCount, triageSummary }: Props) {
  const prodTrend = trends.find(t => t.environment === 'prod');
  const lastPoint = prodTrend?.dataPoints?.slice(-1)[0];

  return (
    <div className="grid grid-cols-4 gap-4">
      <Card>
        <div className="text-xs text-gray-400">PASS RATE (LATEST)</div>
        <div className="text-2xl font-bold text-green-400">{lastPoint ? (lastPoint.passRate * 100).toFixed(1) + '%' : '—'}</div>
      </Card>
      <Card>
        <div className="text-xs text-gray-400">FLAKY TESTS</div>
        <div className="text-2xl font-bold text-yellow-400">{flakyCount}</div>
      </Card>
      <Card>
        <div className="text-xs text-gray-400">UNTRIAGED</div>
        <div className="text-2xl font-bold text-red-400">{triageSummary?.untriaged ?? '—'}</div>
      </Card>
      <Card>
        <div className="text-xs text-gray-400">TOTAL TRIAGED (30d)</div>
        <div className="text-2xl font-bold text-blue-400">{triageSummary?.totalTriaged ?? '—'}</div>
      </Card>
    </div>
  );
}
