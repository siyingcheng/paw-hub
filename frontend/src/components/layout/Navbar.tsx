'use client';
import { useRouter } from "next/navigation";
import { clearAuth } from "@/lib/auth";

export default function Navbar({ projectName }: { projectName?: string }) {
  const router = useRouter();
  const handleLogout = () => { clearAuth(); router.push("/login"); };

  return (
    <nav className="flex items-center justify-between px-6 py-3 bg-gray-900 border-b border-gray-800">
      <div className="flex items-center gap-4">
        <h1 className="text-lg font-bold text-blue-400">Paw-Hub</h1>
        {projectName && <span className="text-gray-400">/ {projectName}</span>}
      </div>
      <button onClick={handleLogout} className="px-4 py-1.5 text-sm rounded-lg bg-gray-800 hover:bg-gray-700">Logout</button>
    </nav>
  );
}
