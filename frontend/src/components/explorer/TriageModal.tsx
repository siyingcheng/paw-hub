"use client"

import { useState } from "react"
import { api } from "@/lib/api"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"

const STATUSES = [
  "UNTRIAGED",
  "NEW_BUG",
  "KNOWN_ISSUE",
  "SCRIPT_ISSUE",
  "DATA_ISSUE",
  "ENV_ISSUE",
  "CR",
  "OTHER",
] as const

interface Props {
  projectId: number
  executionId: number
  currentStatus: string
  currentIssueLink: string
  currentComment: string
  onSaved: () => void
}

export default function TriageModal({
  projectId,
  executionId,
  currentStatus,
  currentIssueLink,
  currentComment,
  onSaved,
}: Props) {
  const [open, setOpen] = useState(false)
  const [status, setStatus] = useState(currentStatus || "UNTRIAGED")
  const [issueLink, setIssueLink] = useState(currentIssueLink || "")
  const [comment, setComment] = useState(currentComment || "")
  const [saving, setSaving] = useState(false)

  const handleSave = async () => {
    setSaving(true)
    try {
      await api.triage.save(projectId, executionId, {
        triageStatus: status,
        issueLink,
        comment,
      })
      toast.success("Triage saved successfully")
      onSaved()
      setOpen(false)
    } catch (e) {
      toast.error(
        "Failed to save triage: " +
          (e instanceof Error ? e.message : "Unknown error")
      )
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <Button
        variant="secondary"
        size="sm"
        className="text-xs"
        onClick={() => setOpen(true)}
      >
        {currentStatus !== "UNTRIAGED"
          ? currentStatus.replace("_", " ")
          : "Triage"}
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Failure Triage</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Status</Label>
              <Select value={status} onValueChange={setStatus}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {STATUSES.map(s => (
                    <SelectItem key={s} value={s}>
                      {s.replace("_", " ")}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Issue Link</Label>
              <Input
                placeholder="Issue link (JIRA URL)"
                value={issueLink}
                onChange={e => setIssueLink(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label>Comment</Label>
              <Textarea
                placeholder="Comment"
                value={comment}
                onChange={e => setComment(e.target.value)}
                rows={2}
              />
            </div>
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleSave} disabled={saving}>
                {saving ? "Saving..." : "Save"}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </>
  )
}
