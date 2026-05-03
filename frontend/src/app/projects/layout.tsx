import RouteGuard from "@/components/layout/RouteGuard"

export default function ProjectsLayout({ children }: { children: React.ReactNode }) {
  return <RouteGuard>{children}</RouteGuard>
}
