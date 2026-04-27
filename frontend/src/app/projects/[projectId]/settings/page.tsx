'use client';
import { useParams } from "next/navigation";
import Navbar from "@/components/layout/Navbar";
import Sidebar from "@/components/layout/Sidebar";

export default function SettingsPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const id = Number(projectId);

  return (
    <div>
      <Navbar projectName="Settings" />
      <div className="flex">
        <Sidebar projectId={id} />
        <main className="flex-1 p-6 space-y-6">
          <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
            <h2 className="text-lg font-semibold mb-4">Project Settings</h2>
            <div className="grid grid-cols-2 gap-4 text-sm">
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
          </div>
          <div className="bg-gray-900 rounded-xl border border-gray-800 p-6">
            <h2 className="text-lg font-semibold mb-4">Team Members</h2>
            <div className="space-y-2">
              <div className="flex justify-between py-2 border-b border-gray-800"><span>Admin User</span><span className="text-purple-400">ADMIN</span></div>
              <div className="flex justify-between py-2 border-b border-gray-800"><span>QA User</span><span className="text-blue-400">QA</span></div>
              <div className="flex justify-between py-2"><span>Viewer User</span><span className="text-gray-400">VIEWER</span></div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
