"use client"

import { useRouter } from "next/navigation"
import Link from "next/link"
import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { ThemeToggle } from "@/components/theme-toggle"
import { clearAuth } from "@/lib/auth"
import { api } from "@/lib/api"
import { getRecentProjects, saveRecentProject, type ProjectItem } from "@/lib/recent-projects"
import { ChevronDown, LogOut } from "lucide-react"

export default function Navbar({
  projectId,
  projectName,
}: {
  projectId?: number
  projectName?: string
}) {
  const router = useRouter()
  const [projects, setProjects] = useState<ProjectItem[]>([])
  const [currentName, setCurrentName] = useState(projectName || "")

  useEffect(() => {
    const recent = getRecentProjects()
    setProjects(recent)

    if (projectId && !projectName) {
      api.project.get(projectId).then(info => {
        setCurrentName(info.name)
        saveRecentProject({ id: projectId, name: info.name })
      }).catch(() => {})
    }
    if (projectId && projectName) {
      saveRecentProject({ id: projectId, name: projectName })
    }
  }, [projectId, projectName])

  const handleLogout = () => {
    clearAuth()
    router.push("/login")
  }

  const handleProjectSwitch = (id: number) => {
    router.push(`/projects/${id}`)
  }

  return (
    <nav className="flex items-center justify-between px-6 py-3 border-b bg-card">
      <div className="flex items-center gap-4">
        <Link href="/projects" className="text-lg font-bold text-primary">
          Paw-Hub
        </Link>
        {projectId && (
          <DropdownMenu>
            <DropdownMenuTrigger className="inline-flex items-center gap-1 rounded-md px-3 py-2 text-sm font-medium text-muted-foreground hover:bg-accent hover:text-accent-foreground">
              / {currentName || `Project #${projectId}`}
              <ChevronDown className="h-4 w-4" />
            </DropdownMenuTrigger>
            <DropdownMenuContent>
              {projects.map(p => (
                <DropdownMenuItem
                  key={p.id}
                  onClick={() => handleProjectSwitch(p.id)}
                >
                  {p.name}
                </DropdownMenuItem>
              ))}
              {projects.length === 0 && (
                <DropdownMenuItem disabled>No recent projects</DropdownMenuItem>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        )}
      </div>
      <div className="flex items-center gap-2">
        <ThemeToggle />
        <Button variant="outline" size="sm" onClick={handleLogout}>
          <LogOut className="h-4 w-4 mr-1" /> Logout
        </Button>
      </div>
    </nav>
  )
}
