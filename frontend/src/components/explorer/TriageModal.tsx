'use client';
import { useState } from "react";
import { api } from "@/lib/api";
import { useToast } from "@/lib/toast";

const STATUSES = ["UNTRIAGED","NEW_BUG","KNOWN_ISSUE","SCRIPT_ISSUE","DATA_ISSUE","ENV_ISSUE","CR","OTHER"];

interface Props {
  projectId: number;
  executionId: number;
  currentStatus: string;
  currentIssueLink: string;
  currentComment: string;
  onSaved: () => void;
}

export default function TriageModal({ projectId, executionId, currentStatus, currentIssueLink, currentComment, onSaved }: Props) {
  const toast = useToast();
  const [open, setOpen] = useState(false);
  const [status, setStatus] = useState(currentStatus || "UNTRIAGED");
  const [issueLink, setIssueLink] = useState(currentIssueLink || "");
  const [comment, setComment] = useState(currentComment || "");
  const [saving, setSaving] = useState(false);

  const handleSave = async () => {
    setSaving(true);
    try {
      await api.triage.save(projectId, executionId, { triageStatus: status, issueLink, comment });
      toast.success("Triage saved successfully");
      onSaved();
      setOpen(false);
    } catch (e) {
      toast.error("Failed to save triage: " + (e instanceof Error ? e.message : "Unknown error"));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <button onClick={() => setOpen(true)} className="text-xs px-2 py-1 rounded bg-gray-700 hover:bg-gray-600">
        {currentStatus !== 'UNTRIAGED' ? currentStatus.replace('_',' ') : 'Triage'}
      </button>
      {open && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
          <div className="bg-gray-900 border border-gray-700 rounded-xl p-6 w-full max-w-md space-y-4">
            <h3 className="font-semibold">Failure Triage</h3>
            <select value={status} onChange={e => setStatus(e.target.value)} className="w-full px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm">
              {STATUSES.map(s => <option key={s} value={s}>{s.replace('_',' ')}</option>)}
            </select>
            <input placeholder="Issue link (JIRA URL)" value={issueLink} onChange={e => setIssueLink(e.target.value)} className="w-full px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm" />
            <textarea placeholder="Comment" value={comment} onChange={e => setComment(e.target.value)} className="w-full px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm" rows={2} />
            <div className="flex gap-2 justify-end">
              <button onClick={() => setOpen(false)} className="px-4 py-2 rounded-lg bg-gray-800 text-sm">Cancel</button>
              <button onClick={handleSave} disabled={saving} className="px-4 py-2 rounded-lg bg-purple-600 text-sm font-semibold">
                {saving ? 'Saving...' : 'Save'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
