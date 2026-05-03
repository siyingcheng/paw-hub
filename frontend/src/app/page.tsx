"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { isAuthenticated } from "@/lib/auth"
import { getLastProjectId } from "@/lib/recent-projects"

export default function Home() {
  const router = useRouter()

  useEffect(() => {
    if (isAuthenticated()) {
      router.replace(`/projects/${getLastProjectId()}`)
      return
    }
    router.replace("/login")
  }, [router])

  return null
}
