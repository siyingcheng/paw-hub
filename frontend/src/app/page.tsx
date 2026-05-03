"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { isAuthenticated } from "@/lib/auth"

export default function Home() {
  const router = useRouter()

  useEffect(() => {
    if (isAuthenticated()) {
      try {
        const recent = JSON.parse(localStorage.getItem("pawhub_recent_projects") || "[]")
        const projectId = recent[0]?.id || 1
        router.replace(`/projects/${projectId}`)
        return
      } catch {}
    }
    router.replace("/login")
  }, [router])

  return null
}
