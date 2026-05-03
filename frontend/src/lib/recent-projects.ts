export interface ProjectItem {
  id: number
  name: string
}

export function getRecentProjects(): ProjectItem[] {
  if (typeof window === "undefined") return []
  try {
    return JSON.parse(localStorage.getItem("pawhub_recent_projects") || "[]")
  } catch {
    return []
  }
}

export function saveRecentProject(project: ProjectItem) {
  const recent = getRecentProjects().filter(p => p.id !== project.id)
  recent.unshift(project)
  localStorage.setItem("pawhub_recent_projects", JSON.stringify(recent.slice(0, 10)))
}

export function getLastProjectId(): number {
  const recent = getRecentProjects()
  return recent[0]?.id || 1
}
