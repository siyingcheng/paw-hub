'use client';
import Link from "next/link";
import { usePathname } from "next/navigation";

const links = [
  { href: "", label: "Dashboard" },
  { href: "/trends", label: "Trends" },
  { href: "/settings", label: "Settings" },
];

export default function Sidebar({ projectId }: { projectId: number }) {
  const pathname = usePathname();
  const base = `/projects/${projectId}`;

  return (
    <aside className="w-56 min-h-[calc(100vh-56px)] bg-gray-900 border-r border-gray-800 p-4">
      <nav className="space-y-1">
        {links.map(link => {
          const active = pathname === base + link.href;
          return (
            <Link key={link.href} href={base + link.href}
              className={`block px-3 py-2 rounded-lg text-sm ${active ? 'bg-blue-600 text-white' : 'text-gray-400 hover:text-white hover:bg-gray-800'}`}>
              {link.label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
