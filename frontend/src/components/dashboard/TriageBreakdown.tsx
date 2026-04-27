import { TriageSummary } from "@/lib/types";
import Card from "@/components/ui/Card";

const CATEGORY_LABELS: Record<string, string> = {
  NEW_BUG: 'New Bug', KNOWN_ISSUE: 'Known Issue', SCRIPT_ISSUE: 'Script Issue',
  DATA_ISSUE: 'Data Issue', ENV_ISSUE: 'Env Issue', CR: 'Code Review', OTHER: 'Other', UNTRIAGED: 'Untriaged',
};

export default function TriageBreakdown({ summary }: { summary: TriageSummary | null }) {
  if (!summary) return <Card><div className="text-xs text-gray-400">TRIAGE BREAKDOWN</div><p className="text-gray-500 text-sm mt-2">No data</p></Card>;

  return (
    <Card>
      <h3 className="text-sm text-gray-400 mb-4">FAILURES BY CATEGORY (30d)</h3>
      <div className="grid grid-cols-4 gap-3">
        {Object.entries(CATEGORY_LABELS).map(([key, label]) => (
          <div key={key} className="flex justify-between text-sm bg-gray-800 rounded-lg px-3 py-2">
            <span className="text-gray-400">{label}</span>
            <span className="text-white font-semibold">{summary.breakdown[key] || 0}</span>
          </div>
        ))}
      </div>
    </Card>
  );
}
