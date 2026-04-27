'use client';
import { useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";

export default function RegisterPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
      await api.auth.register(username, email, password);
      router.push("/login");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center">
      <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-4 bg-gray-900 p-8 rounded-xl border border-gray-800">
        <h1 className="text-2xl font-bold text-center">Register</h1>
        {error && <p className="text-red-400 text-sm">{error}</p>}
        <input className="w-full px-4 py-2 rounded-lg bg-gray-800 border border-gray-700" placeholder="Username" value={username} onChange={e => setUsername(e.target.value)} />
        <input type="email" className="w-full px-4 py-2 rounded-lg bg-gray-800 border border-gray-700" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
        <input type="password" className="w-full px-4 py-2 rounded-lg bg-gray-800 border border-gray-700" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
        <button type="submit" className="w-full py-2 bg-blue-600 hover:bg-blue-700 rounded-lg font-semibold">Register</button>
        <p className="text-sm text-gray-400 text-center">Have an account? <a href="/login" className="text-blue-400">Log in</a></p>
      </form>
    </div>
  );
}
