'use client';
import { useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { setToken } from "@/lib/auth";
import { useToast } from "@/lib/toast";

export default function LoginPage() {
  const router = useRouter();
  const toast = useToast();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await api.auth.login(username, password);
      setToken(res.token);
      toast.success(`Welcome, ${res.username}`);
      router.push("/projects/1");
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center">
      <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-4 bg-gray-900 p-8 rounded-xl border border-gray-800">
        <h1 className="text-2xl font-bold text-center">Paw-Hub</h1>
        <input className="w-full px-4 py-2 rounded-lg bg-gray-800 border border-gray-700" placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} />
        <input type="password" className="w-full px-4 py-2 rounded-lg bg-gray-800 border border-gray-700" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
        <button type="submit" disabled={loading} className="w-full py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 rounded-lg font-semibold">
          {loading ? 'Logging in...' : 'Log In'}
        </button>
        <p className="text-sm text-gray-400 text-center">No account? <a href="/register" className="text-blue-400">Register</a></p>
      </form>
    </div>
  );
}
