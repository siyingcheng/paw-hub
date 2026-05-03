"use client"

import { useEffect, useState, useCallback } from "react"
import { useParams, useRouter } from "next/navigation"
import Navbar from "@/components/layout/Navbar"
import Sidebar from "@/components/layout/Sidebar"
import { api } from "@/lib/api"
import { toast } from "sonner"
import type { ProjectInfo, TeamMember } from "@/lib/types"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Button } from "@/components/ui/button"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { Skeleton } from "@/components/ui/skeleton"

const ROLE_OPTIONS = ["ADMIN", "QA", "VIEWER"] as const

export default function SettingsPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const id = Number(projectId)
  const router = useRouter()

  const [role, setRole] = useState<string>("")
  const [checked, setChecked] = useState(false)
  const [projectInfo, setProjectInfo] = useState<ProjectInfo | null>(null)
  const [members, setMembers] = useState<TeamMember[]>([])
  const [membersLoading, setMembersLoading] = useState(true)
  const [removeTarget, setRemoveTarget] = useState<TeamMember | null>(null)

  const fetchMembers = useCallback(() => {
    api.team
      .listMembers(id)
      .then(setMembers)
      .catch(() => toast.error("Failed to load team members"))
      .finally(() => setMembersLoading(false))
  }, [id])

  useEffect(() => {
    api.auth
      .getRole(id)
      .then(r => {
        setRole(r.role)
        setChecked(true)
        if (r.role !== "ADMIN") {
          toast.error("Only admins can access settings")
          router.push(`/projects/${id}`)
        }
      })
      .catch(() => setChecked(true))

    api.project.get(id).then(setProjectInfo).catch(() => {})
    fetchMembers()
  }, [id])

  const handleRoleChange = (userId: number, newRole: string) => {
    api.team
      .upsertRole(id, userId, newRole)
      .then(updated => {
        setMembers(prev =>
          prev.map(m => (m.userId === userId ? updated : m))
        )
        toast.success(`Role updated to ${newRole}`)
      })
      .catch(err => toast.error(err.message))
  }

  const handleRemoveConfirm = () => {
    if (!removeTarget) return
    api.team
      .removeMember(id, removeTarget.userId)
      .then(() => {
        setMembers(prev =>
          prev.filter(m => m.userId !== removeTarget.userId)
        )
        toast.success(`${removeTarget.username} removed from team`)
      })
      .catch(err => toast.error(err.message))
      .finally(() => setRemoveTarget(null))
  }

  if (!checked || role !== "ADMIN") {
    return (
      <div>
        <Navbar projectId={id} />
        <div className="flex">
          <Sidebar projectId={id} role={role} />
          <main className="flex-1 p-6">
            <div className="flex items-center justify-center h-64 text-muted-foreground">
              Access denied
            </div>
          </main>
        </div>
      </div>
    )
  }

  return (
    <div>
      <Navbar projectId={id} />
      <div className="flex">
        <Sidebar projectId={id} role={role} />
        <main className="flex-1 p-6 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Project Settings</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">Project Name:</span>
                  <span className="ml-2">{projectInfo?.name || "—"}</span>
                </div>
                <div>
                  <span className="text-muted-foreground">Team:</span>
                  <span className="ml-2">
                    {projectInfo?.teamName || "—"}
                  </span>
                </div>
                <div>
                  <span className="text-muted-foreground">API Key:</span>
                  <code className="ml-2 text-xs bg-secondary px-2 py-1 rounded">
                    {projectInfo?.apiKey || "—"}
                  </code>
                </div>
                <div>
                  <span className="text-muted-foreground">
                    Flaky Threshold:
                  </span>
                  <span className="ml-2">0.3</span>
                </div>
                <div>
                  <span className="text-muted-foreground">
                    Regression Sigma:
                  </span>
                  <span className="ml-2">2.0</span>
                </div>
                <div>
                  <span className="text-muted-foreground">
                    Analysis Window:
                  </span>
                  <span className="ml-2">30 days</span>
                </div>
              </div>
              <div className="mt-4 p-3 bg-secondary rounded-lg text-xs">
                <span className="text-muted-foreground">
                  Upload endpoint:{" "}
                </span>
                <code className="text-primary">
                  POST /api/v1/projects/{id}/test-results
                </code>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Team Members</CardTitle>
            </CardHeader>
            <CardContent>
              {membersLoading ? (
                <div className="space-y-2">
                  <Skeleton className="h-10 w-full" />
                  <Skeleton className="h-10 w-full" />
                  <Skeleton className="h-10 w-full" />
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>User</TableHead>
                      <TableHead>Role</TableHead>
                      <TableHead className="w-20" />
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {members.map(m => (
                      <TableRow key={m.userId}>
                        <TableCell>
                          <span>{m.username}</span>
                          <span className="text-xs text-muted-foreground ml-2">
                            {m.email}
                          </span>
                        </TableCell>
                        <TableCell>
                          <Select
                            value={m.role}
                            onValueChange={newRole =>
                              handleRoleChange(m.userId, newRole)
                            }
                          >
                            <SelectTrigger className="w-28">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              {ROLE_OPTIONS.map(r => (
                                <SelectItem key={r} value={r}>
                                  {r}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </TableCell>
                        <TableCell>
                          <Button
                            variant="link"
                            size="sm"
                            className="text-destructive h-auto p-0"
                            onClick={() => setRemoveTarget(m)}
                          >
                            Remove
                          </Button>
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

      <AlertDialog
        open={!!removeTarget}
        onOpenChange={open => !open && setRemoveTarget(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Remove Team Member</AlertDialogTitle>
            <AlertDialogDescription>
              Remove {removeTarget?.username} from the team? This action can be
              undone by re-adding them.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleRemoveConfirm}>
              Remove
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
