"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"
import { Badge } from "@/components/ui/badge"
import { Menu, LayoutDashboard, TrendingUp, Settings, List } from "lucide-react"

interface SidebarLink {
  href: string
  label: string
  icon: React.ReactNode
}

export default function Sidebar({
  projectId,
  role,
}: {
  projectId: number
  role?: string
}) {
  const pathname = usePathname()
  const base = `/projects/${projectId}`

  const allLinks: SidebarLink[] = [
    { href: "", label: "Dashboard", icon: <LayoutDashboard className="h-5 w-5" /> },
    { href: "/runs", label: "Test Runs", icon: <List className="h-5 w-5" /> },
    { href: "/trends", label: "Trends", icon: <TrendingUp className="h-5 w-5" /> },
  ]

  const adminLinks: SidebarLink[] = [
    { href: "/settings", label: "Settings", icon: <Settings className="h-5 w-5" /> },
  ]

  const links: SidebarLink[] =
    role === "ADMIN" ? [...allLinks, ...adminLinks] : allLinks

  const navContent = (
    <div className="flex flex-col h-full p-5">
      <nav className="space-y-1.5 flex-1">
        {links.map(link => {
          const active = pathname === base + link.href
          return (
            <Link key={link.href} href={base + link.href}>
              <Button
                variant={active ? "secondary" : "ghost"}
                className="w-full justify-start gap-2.5"
              >
                {link.icon}
                {link.label}
              </Button>
            </Link>
          )
        })}
      </nav>
      {role && (
        <div className="mt-auto pt-3 border-t">
          <Badge
            variant={
              role === "ADMIN"
                ? "default"
                : role === "QA"
                  ? "secondary"
                  : "outline"
            }
            className="text-xs"
          >
            {role}
          </Badge>
        </div>
      )}
    </div>
  )

  return (
    <>
      <aside className="hidden md:block w-56 min-h-[calc(100vh-56px)] border-r bg-card">
        {navContent}
      </aside>
      <div className="md:hidden fixed top-3 left-3 z-40">
        <Sheet side="left">
          <SheetTrigger className="inline-flex items-center justify-center rounded-lg h-9 w-9 border border-input hover:bg-accent hover:text-accent-foreground">
            <Menu className="h-5 w-5" />
          </SheetTrigger>
          <SheetContent className="w-56 p-0">
            {navContent}
          </SheetContent>
        </Sheet>
      </div>
    </>
  )
}
