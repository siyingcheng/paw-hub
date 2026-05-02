'use client';
import { useEffect, useState, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";
import Navbar from "@/components/layout/Navbar";
import Sidebar from "@/components/layout/Sidebar";
import { api } from "@/lib/api";
import { useToast } from "@/lib/toast";
import type { ProjectInfo, TeamMember } from "@/lib/types";

const ROLE_OPTIONS = ['ADMIN', 'QA', 'VIEWER'] as const;

export default function SettingsPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const id = Number(projectId);
  const router = useRouter();
  const toast = useToast();

  const [role, setRole] = useState<string>("");
  const [checked, setChecked] = useState(false);
  const [projectInfo, setProjectInfo] = useState<ProjectInfo | null>(null);
  const [members, setMembers] = useState<TeamMember[]>([]);
  const [membersLoading, setMembersLoading] = useState(true);

  const fetchMembers = useCallback(() => {
    api.team.listMembers(id)
      .then(setMembers)
      .catch(() => toast.error("Failed to load team members"))
      .finally(() => setMembersLoading(false));
  }, [id]);

  useEffect(() => {
    api.auth.getRole(id).then(r => {
      setRole(r.role);
      setChecked(true);
      if (r.role !== 'ADMIN') {
        toast.error("Only admins can access settings");
        router.push(`/projects/${id}`);
      }
    }).catch(() => setChecked(true));

    api.project.get(id).then(setProjectInfo).catch(() => {});
    fetchMembers();
  }, [id]);

  const handleRoleChange = (userId: number, newRole: string) => {
    api.team.upsertRole(id, userId, newRole)
      .then(updated => {
        setMembers(prev => prev.map(m => m.userId === userId ? updated : m));
        toast.success(`Role updated to ${newRole}`);
      })
      .catch(err => toast.error(err.message));
  };

  const handleRemove = (userId: number, username: string) => {
    if (!confirm(`Remove ${username} from the team?`)) return;
    api.team.removeMember(id, userId)
      .then(() => {
        setMembers(prev => prev.filter(m => m.userId !== userId));
        toast.success(`${username} removed from team`);
      })
      .catch(err => toast.error(err.message));
  };

  if (!checked || role !== 'ADMIN') {
    return (
      <div>
        <Navbar projectName="Settings" />
        <div className="flex">
          <Sidebar projectId={id} role={role} />
          <main className="flex-1 p-6">
            <div className="flex items-center justify-center h-64 text-gray-400">Access denied</div>
          </main>
        </div>
      </div>
    );
  }

  return (
    <div>
      <Navbar projectName="Settings" />
      <div className="flex">
        <Sidebar projectId={id} role={role} />
        <main className="flex-1 p-6 space-y-6">
          <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
            <h2 className="text-lg font-semibold mb-4">Project Settings</h2>
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span className="text-gray-400">Project Name:</span>
                <span className="ml-2">{projectInfo?.name || '—'}</span>
              </div>
              <div>
                <span className="text-gray-400">Organization:</span>
                <span className="ml-2">{projectInfo?.orgName || '—'}</span>
              </div>
              <div>
                <span className="text-gray-400">Team:</span>
                <span className="ml-2">{projectInfo?.teamName || '—'}</span>
              </div>
              <div>
                <span className="text-gray-400">API Key:</span>
                <code className="ml-2 text-xs bg-gray-800 px-2 py-1 rounded">{projectInfo?.apiKey || '—'}</code>
              </div>
              <div>
                <span className="text-gray-400">Analysis Flaky Threshold:</span>
                <span className="ml-2">0.3</span>
              </div>
              <div>
                <span className="text-gray-400">Regression Sigma:</span>
                <span className="ml-2">2.0</span>
              </div>
              <div>
                <span className="text-gray-400">Analysis Window:</span>
                <span className="ml-2">30 days</span>
              </div>
            </div>
            <div className="mt-4 p-3 bg-gray-800 rounded-lg text-xs">
              <span className="text-gray-400">Upload endpoint: </span>
              <code className="text-blue-400">POST /api/v1/projects/{id}/test-results</code>
            </div>
          </div>
          <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
            <h2 className="text-lg font-semibold mb-4">Team Members</h2>
            {membersLoading ? (
              <div className="text-gray-400 text-sm">Loading members...</div>
            ) : (
              <div className="space-y-1">
                {members.map(m => (
                  <div key={m.userId} className="flex items-center justify-between py-2 border-b border-gray-800 last:border-b-0">
                    <div>
                      <span className="text-sm">{m.username}</span>
                      <span className="text-xs text-gray-500 ml-2">{m.email}</span>
                    </div>
                    <div className="flex items-center gap-3">
                      <select
                        value={m.role}
                        onChange={e => handleRoleChange(m.userId, e.target.value)}
                        className="bg-gray-800 border border-gray-700 rounded px-2 py-1 text-xs text-gray-200 focus:outline-none focus:border-blue-500"
                      >
                        {ROLE_OPTIONS.map(r => (
                          <option key={r} value={r}>{r}</option>
                        ))}
                      </select>
                      <button
                        onClick={() => handleRemove(m.userId, m.username)}
                        className="text-xs text-red-400 hover:text-red-300 transition-colors"
                      >
                        Remove
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </main>
      </div>
    </div>
  );
}
