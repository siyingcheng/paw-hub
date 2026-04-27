'use client';
import Link from "next/link";
import { usePathname } from "next/navigation";

interface Props {
  projectId: number;
  role?: string;
}

export default function Sidebar({ projectId, role }: Props) {
  const pathname = usePathname();
  const base = `/projects/${projectId}`;

  const allLinks = [
    { href: "", label: "Dashboard" },
    { href: "/trends", label: "Trends" },
  ];

  const adminLinks = [
    { href: "/settings", label: "Settings" },
  ];

  const links = role === 'ADMIN' ? [...allLinks, ...adminLinks] : allLinks;

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
      {role && (
        <div className="mt-4 pt-3 border-t border-gray-800">
          <span className={`text-xs px-2 py-1 rounded ${role === 'ADMIN' ? 'bg-purple-900 text-purple-400' : role === 'QA' ? 'bg-blue-900 text-blue-400' : 'bg-gray-800 text-gray-400'}`}>
            {role}
          </span>
        </div>
      )}
    </aside>
  );
}
