# shadcn/ui Frontend Refactor — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace all custom Tailwind components with shadcn/ui, add theme switching, project switcher dropdown, mobile-responsive sidebar, and accessible dialogs.

**Architecture:** Keep Next.js 15 App Router, Recharts, API layer unchanged. Layer shadcn/ui (Radix primitives + styled components) on top of Tailwind CSS 4. Theme via next-themes, toasts via sonner.

**Tech Stack:** Next.js 15, React 19, Tailwind CSS 4, shadcn/ui 2.x, next-themes, sonner, Recharts 2.x

---

### Task 1: Initialize shadcn/ui and install dependencies

**Files:**
- Create: `frontend/components.json`
- Modify: `frontend/package.json`
- Create: `frontend/src/lib/utils.ts`

- [ ] **Step 1: Run shadcn/ui init**

```bash
cd frontend && npx shadcn@latest init --defaults --src-dir src
```

Expected: Creates `components.json`, installs base deps (`clsx`, `tailwind-merge`, `class-variance-authority`, `lucide-react`, `tailwindcss-animate`), updates `package.json`. May update `globals.css` with CSS variables (if it does, accept the changes for now — we'll rewrite it in Task 3).

- [ ] **Step 2: Install next-themes and sonner**

```bash
cd frontend && npm install next-themes sonner
```

Expected: Adds `next-themes` and `sonner` to `package.json` dependencies.

- [ ] **Step 3: Create `src/lib/utils.ts` with the `cn` helper**

Write `frontend/src/lib/utils.ts`:

```typescript
import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
```

- [ ] **Step 4: Verify base deps installed correctly**

```bash
cd frontend && node -e "require('clsx'); require('tailwind-merge'); require('class-variance-authority'); console.log('All base deps resolve')"
```

Expected: "All base deps resolve"

- [ ] **Step 5: Commit**

```bash
git add frontend/components.json frontend/package.json frontend/package-lock.json frontend/src/lib/utils.ts
git commit -m "feat: initialize shadcn/ui with base dependencies and cn utility"
```

---

### Task 2: Create theme infrastructure

**Files:**
- Create: `frontend/src/components/theme-provider.tsx`
- Create: `frontend/src/components/theme-toggle.tsx`

- [ ] **Step 1: Create ThemeProvider wrapper**

Write `frontend/src/components/theme-provider.tsx`:

```typescript
"use client"

import { ThemeProvider as NextThemesProvider } from "next-themes"
import type { ComponentProps } from "react"

export function ThemeProvider({ children, ...props }: ComponentProps<typeof NextThemesProvider>) {
  return <NextThemesProvider {...props}>{children}</NextThemesProvider>
}
```

- [ ] **Step 2: Create ThemeToggle component**

Write `frontend/src/components/theme-toggle.tsx`:

```typescript
"use client"

import { Moon, Sun, Monitor } from "lucide-react"
import { useTheme } from "next-themes"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

export function ThemeToggle() {
  const { setTheme } = useTheme()

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon">
          <Sun className="h-5 w-5 rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
          <Moon className="absolute h-5 w-5 rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
          <span className="sr-only">Toggle theme</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={() => setTheme("light")}>
          <Sun className="mr-2 h-4 w-4" /> Light
        </DropdownMenuItem>
        <DropdownMenuItem onClick={() => setTheme("dark")}>
          <Moon className="mr-2 h-4 w-4" /> Dark
        </DropdownMenuItem>
        <DropdownMenuItem onClick={() => setTheme("system")}>
          <Monitor className="mr-2 h-4 w-4" /> System
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/theme-provider.tsx frontend/src/components/theme-toggle.tsx
git commit -m "feat: add ThemeProvider wrapper and ThemeToggle component"
```

---

### Task 3: Rewrite globals.css and update root layout

**Files:**
- Modify: `frontend/src/app/globals.css`
- Modify: `frontend/src/app/layout.tsx`

- [ ] **Step 1: Rewrite globals.css with shadcn/ui CSS variables (hybrid dark theme)**

Write `frontend/src/app/globals.css`:

```css
@import "tailwindcss";

@custom-variant dark (&:is(.dark *));

@theme inline {
  --color-background: var(--background);
  --color-foreground: var(--foreground);
  --color-card: var(--card);
  --color-card-foreground: var(--card-foreground);
  --color-primary: var(--primary);
  --color-primary-foreground: var(--primary-foreground);
  --color-secondary: var(--secondary);
  --color-secondary-foreground: var(--secondary-foreground);
  --color-muted: var(--muted);
  --color-muted-foreground: var(--muted-foreground);
  --color-accent: var(--accent);
  --color-accent-foreground: var(--accent-foreground);
  --color-destructive: var(--destructive);
  --color-destructive-foreground: var(--destructive-foreground);
  --color-border: var(--border);
  --color-input: var(--input);
  --color-ring: var(--ring);
  --color-success: var(--success);
  --color-success-foreground: var(--success-foreground);
  --color-warning: var(--warning);
  --color-warning-foreground: var(--warning-foreground);
  --radius-sm: calc(var(--radius) - 4px);
  --radius-md: calc(var(--radius) - 2px);
  --radius-lg: var(--radius);
}

:root {
  --background: #ffffff;
  --foreground: #0a0a0a;
  --card: #ffffff;
  --card-foreground: #0a0a0a;
  --primary: #2563eb;
  --primary-foreground: #ffffff;
  --secondary: #f4f4f5;
  --secondary-foreground: #18181b;
  --muted: #f4f4f5;
  --muted-foreground: #71717a;
  --accent: #f4f4f5;
  --accent-foreground: #18181b;
  --destructive: #ef4444;
  --destructive-foreground: #ffffff;
  --border: #e4e4e7;
  --input: #e4e4e7;
  --ring: #2563eb;
  --success: #22c55e;
  --success-foreground: #ffffff;
  --warning: #f59e0b;
  --warning-foreground: #0a0a0a;
  --radius: 0.5rem;
}

.dark {
  --background: #030712;
  --foreground: #e5e5e5;
  --card: #111111;
  --card-foreground: #e5e5e5;
  --primary: #3b82f6;
  --primary-foreground: #ffffff;
  --secondary: #1f2937;
  --secondary-foreground: #e5e5e5;
  --muted: #1f2937;
  --muted-foreground: #9ca3af;
  --accent: #1f2937;
  --accent-foreground: #e5e5e5;
  --destructive: #ef4444;
  --destructive-foreground: #ffffff;
  --border: #1f2937;
  --input: #1f2937;
  --ring: #3b82f6;
  --success: #22c55e;
  --success-foreground: #ffffff;
  --warning: #f59e0b;
  --warning-foreground: #0a0a0a;
}

* {
  border-color: var(--border);
}

body {
  background-color: var(--background);
  color: var(--foreground);
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(100px); }
  to { opacity: 1; transform: translateX(0); }
}
```

- [ ] **Step 2: Update root layout to add ThemeProvider and Toaster**

Write `frontend/src/app/layout.tsx`:

```typescript
import type { Metadata } from "next"
import { ThemeProvider } from "@/components/theme-provider"
import { Toaster } from "sonner"
import "./globals.css"

export const metadata: Metadata = {
  title: "Paw-Hub",
  description: "Test Result Review Application",
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className="min-h-screen">
        <ThemeProvider
          attribute="class"
          defaultTheme="system"
          enableSystem
          disableTransitionOnChange
        >
          {children}
          <Toaster richColors position="bottom-right" />
        </ThemeProvider>
      </body>
    </html>
  )
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/app/globals.css frontend/src/app/layout.tsx
git commit -m "feat: add shadcn/ui CSS variables, ThemeProvider, and Sonner Toaster"
```

---

### Task 4: Install shadcn/ui component files

**Files:**
- Create: `frontend/src/components/ui/button.tsx`
- Create: `frontend/src/components/ui/input.tsx`
- Create: `frontend/src/components/ui/card.tsx`
- Create: `frontend/src/components/ui/badge.tsx`
- Create: `frontend/src/components/ui/select.tsx`
- Create: `frontend/src/components/ui/dialog.tsx`
- Create: `frontend/src/components/ui/table.tsx`
- Create: `frontend/src/components/ui/dropdown-menu.tsx`
- Create: `frontend/src/components/ui/sheet.tsx`
- Create: `frontend/src/components/ui/skeleton.tsx`
- Create: `frontend/src/components/ui/alert.tsx`
- Create: `frontend/src/components/ui/alert-dialog.tsx`
- Create: `frontend/src/components/ui/textarea.tsx`
- Create: `frontend/src/components/ui/label.tsx`
- Create: `frontend/src/components/ui/separator.tsx`

- [ ] **Step 1: Install all shadcn/ui components**

```bash
cd frontend && npx shadcn@latest add button input card badge select dialog table dropdown-menu sheet skeleton alert alert-dialog textarea label separator --yes
```

Expected: Creates all component files in `src/components/ui/`. Each file is self-contained with the component and its Radix imports.

- [ ] **Step 2: Verify all component files exist**

Run:
```bash
ls frontend/src/components/ui/
```

Expected output includes: `button.tsx`, `input.tsx`, `card.tsx`, `badge.tsx`, `select.tsx`, `dialog.tsx`, `table.tsx`, `dropdown-menu.tsx`, `sheet.tsx`, `skeleton.tsx`, `alert.tsx`, `alert-dialog.tsx`, `textarea.tsx`, `label.tsx`, `separator.tsx`

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/ui/ frontend/package.json frontend/package-lock.json
git commit -m "feat: add shadcn/ui component library files (Button, Input, Card, Badge, Select, Dialog, Table, DropdownMenu, Sheet, Skeleton, Alert, AlertDialog, Textarea, Label, Separator)"
```

---

### Task 5: Refactor Navbar (theme toggle + project switcher)

**Files:**
- Modify: `frontend/src/components/layout/Navbar.tsx`

- [ ] **Step 1: Rewrite Navbar with shadcn/ui Button, DropdownMenu for project switcher and theme toggle**

Write `frontend/src/components/layout/Navbar.tsx`:

```typescript
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
import { ChevronDown, LogOut } from "lucide-react"

interface ProjectItem {
  id: number
  name: string
}

function getRecentProjects(): ProjectItem[] {
  if (typeof window === "undefined") return []
  try {
    return JSON.parse(localStorage.getItem("pawhub_recent_projects") || "[]")
  } catch {
    return []
  }
}

function saveRecentProject(project: ProjectItem) {
  const recent = getRecentProjects().filter(p => p.id !== project.id)
  recent.unshift(project)
  localStorage.setItem("pawhub_recent_projects", JSON.stringify(recent.slice(0, 10)))
}

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
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="gap-1 text-muted-foreground">
                / {currentName || `Project #${projectId}`}
                <ChevronDown className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="start">
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
```

- [ ] **Step 2: Verify Navbar type-checks**

```bash
cd frontend && npx tsc --noEmit src/components/layout/Navbar.tsx
```

Expected: No errors (may show warnings for unrelated files — that's OK for now).

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/layout/Navbar.tsx
git commit -m "feat: refactor Navbar with shadcn/ui Button, DropdownMenu project switcher, and ThemeToggle"
```

---

### Task 6: Refactor Sidebar (mobile Sheet)

**Files:**
- Modify: `frontend/src/components/layout/Sidebar.tsx`

- [ ] **Step 1: Rewrite Sidebar with shadcn/ui Button variants and Sheet for mobile**

Write `frontend/src/components/layout/Sidebar.tsx`:

```typescript
"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"
import { Badge } from "@/components/ui/badge"
import { Menu, LayoutDashboard, TrendingUp, Settings } from "lucide-react"

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
    { href: "", label: "Dashboard", icon: <LayoutDashboard className="h-4 w-4" /> },
    { href: "/trends", label: "Trends", icon: <TrendingUp className="h-4 w-4" /> },
  ]

  const adminLinks: SidebarLink[] = [
    { href: "/settings", label: "Settings", icon: <Settings className="h-4 w-4" /> },
  ]

  const links: SidebarLink[] =
    role === "ADMIN" ? [...allLinks, ...adminLinks] : allLinks

  const navContent = (
    <div className="flex flex-col h-full p-4">
      <nav className="space-y-1 flex-1">
        {links.map(link => {
          const active = pathname === base + link.href
          return (
            <Link key={link.href} href={base + link.href}>
              <Button
                variant={active ? "secondary" : "ghost"}
                size="sm"
                className="w-full justify-start gap-2"
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
      {/* Desktop sidebar */}
      <aside className="hidden md:block w-56 min-h-[calc(100vh-56px)] border-r bg-card">
        {navContent}
      </aside>
      {/* Mobile sidebar via Sheet */}
      <div className="md:hidden fixed top-3 left-3 z-40">
        <Sheet>
          <SheetTrigger asChild>
            <Button variant="outline" size="icon">
              <Menu className="h-5 w-5" />
            </Button>
          </SheetTrigger>
          <SheetContent side="left" className="w-56 p-0">
            {navContent}
          </SheetContent>
        </Sheet>
      </div>
    </>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/layout/Sidebar.tsx
git commit -m "feat: refactor Sidebar with shadcn/ui ghost buttons and mobile Sheet"
```

---

### Task 7: Refactor login page

**Files:**
- Modify: `frontend/src/app/login/page.tsx`

- [ ] **Step 1: Rewrite login page with shadcn/ui components**

Write `frontend/src/app/login/page.tsx`:

```typescript
"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import { api } from "@/lib/api"
import { setToken } from "@/lib/auth"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

function getLastProjectId(): number {
  if (typeof window === "undefined") return 1
  try {
    const recent = JSON.parse(localStorage.getItem("pawhub_recent_projects") || "[]")
    return recent[0]?.id || 1
  } catch {
    return 1
  }
}

export default function LoginPage() {
  const router = useRouter()
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await api.auth.login(username, password)
      setToken(res.token)
      toast.success(`Welcome, ${res.username}`)
      router.push(`/projects/${getLastProjectId()}`)
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Login failed")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle className="text-center text-2xl">Paw-Hub</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username">Username</Label>
              <Input
                id="username"
                placeholder="Username"
                value={username}
                onChange={e => setUsername(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                placeholder="Password"
                value={password}
                onChange={e => setPassword(e.target.value)}
              />
            </div>
            <Button type="submit" disabled={loading} className="w-full">
              {loading ? "Logging in..." : "Log In"}
            </Button>
            <p className="text-sm text-muted-foreground text-center">
              No account?{" "}
              <Link href="/register" className="text-primary hover:underline">
                Register
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/app/login/page.tsx
git commit -m "feat: refactor login page with shadcn/ui Card, Input, Label, Button"
```

---

### Task 8: Refactor register page

**Files:**
- Modify: `frontend/src/app/register/page.tsx`

- [ ] **Step 1: Rewrite register page with shadcn/ui components**

Write `frontend/src/app/register/page.tsx`:

```typescript
"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import { api } from "@/lib/api"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

export default function RegisterPage() {
  const router = useRouter()
  const [username, setUsername] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      await api.auth.register(username, email, password)
      toast.success("Registration successful! Please log in.")
      router.push("/login")
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Registration failed")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle className="text-center text-2xl">Register</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username">Username</Label>
              <Input
                id="username"
                placeholder="Username"
                value={username}
                onChange={e => setUsername(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="Email"
                value={email}
                onChange={e => setEmail(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                placeholder="Password"
                value={password}
                onChange={e => setPassword(e.target.value)}
              />
            </div>
            <Button type="submit" disabled={loading} className="w-full">
              {loading ? "Registering..." : "Register"}
            </Button>
            <p className="text-sm text-muted-foreground text-center">
              Have an account?{" "}
              <Link href="/login" className="text-primary hover:underline">
                Log in
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/app/register/page.tsx
git commit -m "feat: refactor register page with shadcn/ui Card, Input, Label, Button"
```

---

### Task 9: Refactor KpiCards dashboard component

**Files:**
- Modify: `frontend/src/components/dashboard/KpiCards.tsx`

- [ ] **Step 1: Rewrite KpiCards with shadcn/ui Card components**

Write `frontend/src/components/dashboard/KpiCards.tsx`:

```typescript
import { TrendResponse, TriageSummary } from "@/lib/types"
import { Card, CardContent } from "@/components/ui/card"

interface Props {
  trends: TrendResponse[]
  flakyCount: number
  triageSummary: TriageSummary | null
}

export default function KpiCards({ trends, flakyCount, triageSummary }: Props) {
  const prodTrend = trends.find(t => t.environment === "prod")
  const lastPoint = prodTrend?.dataPoints?.slice(-1)[0]

  const items = [
    {
      label: "PASS RATE (LATEST)",
      value: lastPoint ? (lastPoint.passRate * 100).toFixed(1) + "%" : "—",
      color: "text-green-500",
    },
    {
      label: "FLAKY TESTS",
      value: String(flakyCount),
      color: "text-yellow-500",
    },
    {
      label: "UNTRIAGED",
      value: triageSummary?.untriaged ?? "—",
      color: "text-red-500",
    },
    {
      label: "TOTAL TRIAGED (30d)",
      value: triageSummary?.totalTriaged ?? "—",
      color: "text-blue-500",
    },
  ]

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      {items.map(item => (
        <Card key={item.label}>
          <CardContent className="p-4">
            <div className="text-xs text-muted-foreground">{item.label}</div>
            <div className={`text-2xl font-bold mt-1 ${item.color}`}>
              {item.value}
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/dashboard/KpiCards.tsx
git commit -m "feat: refactor KpiCards with shadcn/ui Card component"
```

---

### Task 10: Refactor PassRateChart dashboard component

**Files:**
- Modify: `frontend/src/components/dashboard/PassRateChart.tsx`

- [ ] **Step 1: Rewrite PassRateChart with shadcn/ui Card wrapper**

Write `frontend/src/components/dashboard/PassRateChart.tsx`:

```typescript
"use client"

import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts"
import { TrendResponse } from "@/lib/types"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

interface Props {
  trends: TrendResponse[]
}

const COLORS: Record<string, string> = {
  dev: "#3b82f6",
  staging: "#f59e0b",
  prod: "#22c55e",
}

export default function PassRateChart({ trends }: Props) {
  if (!trends.length) {
    return (
      <Card>
        <CardContent className="p-4 text-muted-foreground">
          No trend data
        </CardContent>
      </Card>
    )
  }

  const dateMap = new Map<string, Record<string, number>>()
  trends.forEach(t => {
    t.dataPoints.forEach(dp => {
      const rec = dateMap.get(dp.date) || {}
      rec[t.environment] = dp.passRate * 100
      dateMap.set(dp.date, rec)
    })
  })
  const data = Array.from(dateMap.entries()).map(([date, envs]) => ({
    date,
    ...envs,
  }))

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-sm text-muted-foreground">
          PASS RATE TREND
        </CardTitle>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={250}>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
            <XAxis dataKey="date" stroke="#94a3b8" fontSize={12} />
            <YAxis stroke="#94a3b8" fontSize={12} domain={[80, 100]} />
            <Tooltip />
            <Legend />
            {["dev", "staging", "prod"].map(env => (
              <Line
                key={env}
                type="monotone"
                dataKey={env}
                stroke={COLORS[env]}
                strokeWidth={2}
                dot={false}
                connectNulls
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/dashboard/PassRateChart.tsx
git commit -m "feat: refactor PassRateChart with shadcn/ui Card wrapper"
```

---

### Task 11: Refactor RecentRegressions dashboard component

**Files:**
- Modify: `frontend/src/components/dashboard/RecentRegressions.tsx`

- [ ] **Step 1: Rewrite RecentRegressions with shadcn/ui Card, Alert, Badge**

Write `frontend/src/components/dashboard/RecentRegressions.tsx`:

```typescript
"use client"

import { useEffect, useState } from "react"
import { api } from "@/lib/api"
import { RegressionResponse } from "@/lib/types"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Badge } from "@/components/ui/badge"
import { AlertTriangle, Diamond } from "lucide-react"

interface Props {
  projectId: number
}

export default function RecentRegressions({ projectId }: Props) {
  const [regressions, setRegressions] = useState<RegressionResponse[]>([])

  useEffect(() => {
    api.analysis.getRegressions(projectId).then(r => setRegressions(r))
  }, [projectId])

  const allCases = regressions.flatMap(r => r.regressedCases)
  const hasData =
    regressions.some(r => r.runLevelRegression) || allCases.length > 0

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-sm text-muted-foreground">
          RECENT REGRESSIONS
        </CardTitle>
      </CardHeader>
      <CardContent>
        {!hasData ? (
          <p className="text-muted-foreground text-sm">
            No regressions detected
          </p>
        ) : (
          <div className="space-y-3">
            {regressions
              .filter(r => r.runLevelRegression)
              .map((r, i) => (
                <Alert key={`run-${i}`} variant="destructive">
                  <AlertTriangle className="h-4 w-4" />
                  <AlertDescription>
                    <div className="font-medium text-sm">
                      Run-level regression
                    </div>
                    {r.runLevelDetail && (
                      <div className="text-xs opacity-80 mt-0.5">
                        {r.runLevelDetail}
                      </div>
                    )}
                  </AlertDescription>
                </Alert>
              ))}
            {allCases.slice(0, 5).map((c, i) => (
              <div
                key={`case-${i}`}
                className="flex items-center gap-2 text-sm"
              >
                <Diamond className="h-3 w-3 text-red-500" />
                <span className="truncate">{c}</span>
                <Badge variant="destructive" className="ml-auto text-xs">
                  regressed
                </Badge>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/dashboard/RecentRegressions.tsx
git commit -m "feat: refactor RecentRegressions with shadcn/ui Card, Alert, Badge"
```

---

### Task 12: Refactor TriageBreakdown dashboard component

**Files:**
- Modify: `frontend/src/components/dashboard/TriageBreakdown.tsx`

- [ ] **Step 1: Rewrite TriageBreakdown with shadcn/ui Card and inline grid**

Write `frontend/src/components/dashboard/TriageBreakdown.tsx`:

```typescript
import { TriageSummary } from "@/lib/types"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

const CATEGORY_LABELS: Record<string, string> = {
  NEW_BUG: "New Bug",
  KNOWN_ISSUE: "Known Issue",
  SCRIPT_ISSUE: "Script Issue",
  DATA_ISSUE: "Data Issue",
  ENV_ISSUE: "Env Issue",
  CR: "Code Review",
  OTHER: "Other",
  UNTRIAGED: "Untriaged",
}

export default function TriageBreakdown({
  summary,
}: {
  summary: TriageSummary | null
}) {
  if (!summary) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="text-sm text-muted-foreground">
            TRIAGE BREAKDOWN
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-muted-foreground text-sm">No data</p>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-sm text-muted-foreground">
          FAILURES BY CATEGORY (30d)
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
          {Object.entries(CATEGORY_LABELS).map(([key, label]) => (
            <div
              key={key}
              className="flex justify-between text-sm bg-secondary rounded-lg px-3 py-2"
            >
              <span className="text-muted-foreground">{label}</span>
              <span className="font-semibold">
                {summary.breakdown[key] || 0}
              </span>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/dashboard/TriageBreakdown.tsx
git commit -m "feat: refactor TriageBreakdown with shadcn/ui Card component"
```

---

### Task 13: Refactor FilterBar explorer component

**Files:**
- Modify: `frontend/src/components/explorer/FilterBar.tsx`

- [ ] **Step 1: Rewrite FilterBar with shadcn/ui Input and Select**

Write `frontend/src/components/explorer/FilterBar.tsx`:

```typescript
"use client"

import { Input } from "@/components/ui/input"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

interface Props {
  search: string
  onSearchChange: (v: string) => void
  statusFilter: string
  onStatusChange: (v: string) => void
  envFilter: string
  onEnvChange: (v: string) => void
}

export default function FilterBar({
  search,
  onSearchChange,
  statusFilter,
  onStatusChange,
  envFilter,
  onEnvChange,
}: Props) {
  return (
    <div className="flex gap-3 mb-4 flex-wrap">
      <Input
        className="flex-1 min-w-[200px]"
        placeholder="Search test name..."
        value={search}
        onChange={e => onSearchChange(e.target.value)}
      />
      <Select
        value={statusFilter}
        onValueChange={onStatusChange}
      >
        <SelectTrigger className="w-[150px]">
          <SelectValue placeholder="All statuses" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="all">All statuses</SelectItem>
          <SelectItem value="FAIL">FAIL</SelectItem>
          <SelectItem value="PASS">PASS</SelectItem>
          <SelectItem value="SKIP">SKIP</SelectItem>
          <SelectItem value="ERROR">ERROR</SelectItem>
        </SelectContent>
      </Select>
      <Select value={envFilter} onValueChange={onEnvChange}>
        <SelectTrigger className="w-[130px]">
          <SelectValue placeholder="All envs" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="all">All envs</SelectItem>
          <SelectItem value="dev">dev</SelectItem>
          <SelectItem value="staging">staging</SelectItem>
          <SelectItem value="prod">prod</SelectItem>
        </SelectContent>
      </Select>
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/explorer/FilterBar.tsx
git commit -m "feat: refactor FilterBar with shadcn/ui Input and Select"
```

---

### Task 14: Refactor ErrorDetail explorer component

**Files:**
- Modify: `frontend/src/components/explorer/ErrorDetail.tsx`

- [ ] **Step 1: Rewrite ErrorDetail with shadcn/ui Alert and collapsible state**

Write `frontend/src/components/explorer/ErrorDetail.tsx`:

```typescript
"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Alert, AlertDescription } from "@/components/ui/alert"

interface Props {
  errorMessage?: string
  stackTrace?: string
  errorType?: string
}

export default function ErrorDetail({
  errorMessage,
  stackTrace,
  errorType,
}: Props) {
  const [open, setOpen] = useState(false)
  if (!errorMessage) return null

  return (
    <div>
      <Button
        variant="link"
        size="sm"
        className="text-xs text-red-500 h-auto p-0"
        onClick={() => setOpen(!open)}
      >
        {open ? "Hide" : "Details"}
      </Button>
      {open && (
        <Alert variant="destructive" className="mt-2">
          <AlertDescription className="text-xs font-mono">
            {errorType && (
              <div className="text-red-400 mb-1 font-semibold">
                {errorType}
              </div>
            )}
            <div className="text-red-300 mb-1">{errorMessage}</div>
            {stackTrace && (
              <pre className="text-muted-foreground whitespace-pre-wrap max-h-40 overflow-y-auto">
                {stackTrace}
              </pre>
            )}
          </AlertDescription>
        </Alert>
      )}
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/explorer/ErrorDetail.tsx
git commit -m "feat: refactor ErrorDetail with shadcn/ui Alert and Button link variant"
```

---

### Task 15: Refactor TriageModal explorer component

**Files:**
- Modify: `frontend/src/components/explorer/TriageModal.tsx`

- [ ] **Step 1: Rewrite TriageModal with shadcn/ui Dialog, Select, Input, Textarea, Button**

Write `frontend/src/components/explorer/TriageModal.tsx`:

```typescript
"use client"

import { useState } from "react"
import { api } from "@/lib/api"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"

const STATUSES = [
  "UNTRIAGED",
  "NEW_BUG",
  "KNOWN_ISSUE",
  "SCRIPT_ISSUE",
  "DATA_ISSUE",
  "ENV_ISSUE",
  "CR",
  "OTHER",
] as const

interface Props {
  projectId: number
  executionId: number
  currentStatus: string
  currentIssueLink: string
  currentComment: string
  onSaved: () => void
}

export default function TriageModal({
  projectId,
  executionId,
  currentStatus,
  currentIssueLink,
  currentComment,
  onSaved,
}: Props) {
  const [open, setOpen] = useState(false)
  const [status, setStatus] = useState(currentStatus || "UNTRIAGED")
  const [issueLink, setIssueLink] = useState(currentIssueLink || "")
  const [comment, setComment] = useState(currentComment || "")
  const [saving, setSaving] = useState(false)

  const handleSave = async () => {
    setSaving(true)
    try {
      await api.triage.save(projectId, executionId, {
        triageStatus: status,
        issueLink,
        comment,
      })
      toast.success("Triage saved successfully")
      onSaved()
      setOpen(false)
    } catch (e) {
      toast.error(
        "Failed to save triage: " +
          (e instanceof Error ? e.message : "Unknown error")
      )
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <Button
        variant="secondary"
        size="sm"
        className="text-xs"
        onClick={() => setOpen(true)}
      >
        {currentStatus !== "UNTRIAGED"
          ? currentStatus.replace("_", " ")
          : "Triage"}
      </Button>
      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Failure Triage</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Status</Label>
              <Select value={status} onValueChange={setStatus}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {STATUSES.map(s => (
                    <SelectItem key={s} value={s}>
                      {s.replace("_", " ")}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Issue Link</Label>
              <Input
                placeholder="Issue link (JIRA URL)"
                value={issueLink}
                onChange={e => setIssueLink(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label>Comment</Label>
              <Textarea
                placeholder="Comment"
                value={comment}
                onChange={e => setComment(e.target.value)}
                rows={2}
              />
            </div>
            <div className="flex gap-2 justify-end">
              <Button variant="outline" onClick={() => setOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleSave} disabled={saving}>
                {saving ? "Saving..." : "Save"}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/explorer/TriageModal.tsx
git commit -m "feat: refactor TriageModal with shadcn/ui Dialog, Select, Input, Textarea, Button"
```

---

### Task 16: Refactor TestTable explorer component

**Files:**
- Modify: `frontend/src/components/explorer/TestTable.tsx`

- [ ] **Step 1: Rewrite TestTable with shadcn/ui Table and Badge**

Write `frontend/src/components/explorer/TestTable.tsx`:

```typescript
"use client"

import { useEffect, useState } from "react"
import { TestExecution, TriageResponse } from "@/lib/types"
import { api } from "@/lib/api"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import ErrorDetail from "./ErrorDetail"
import TriageModal from "./TriageModal"

interface Props {
  projectId: number
  executions: TestExecution[]
  onTriageSaved: () => void
}

function statusVariant(
  status: string
): "default" | "secondary" | "destructive" | "outline" {
  switch (status) {
    case "PASS":
      return "default"
    case "FAIL":
      return "destructive"
    case "SKIP":
      return "secondary"
    case "ERROR":
      return "destructive"
    default:
      return "outline"
  }
}

export default function TestTable({
  projectId,
  executions,
  onTriageSaved,
}: Props) {
  const [triages, setTriages] = useState<Map<number, TriageResponse>>(new Map())

  useEffect(() => {
    Promise.all(executions.map(e => api.triage.get(projectId, e.id))).then(
      tList => {
        const map = new Map<number, TriageResponse>()
        tList.forEach((t, i) => {
          if (t) map.set(executions[i].id, t)
        })
        setTriages(map)
      }
    )
  }, [executions, projectId])

  const handleTriageSaved = () => {
    onTriageSaved()
    Promise.all(executions.map(e => api.triage.get(projectId, e.id))).then(
      tList => {
        const map = new Map<number, TriageResponse>()
        tList.forEach((t, i) => {
          if (t) map.set(executions[i].id, t)
        })
        setTriages(map)
      }
    )
  }

  return (
    <div className="rounded-md border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead className="w-10">#</TableHead>
            <TableHead>Case ID</TableHead>
            <TableHead>Suite / Class</TableHead>
            <TableHead>Test</TableHead>
            <TableHead className="w-12">Att</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Duration</TableHead>
            <TableHead>Error</TableHead>
            <TableHead>Triage</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {executions.map((e, i) => {
            const t = triages.get(e.id)
            const isFailure = e.status === "FAIL" || e.status === "ERROR"
            return (
              <TableRow
                key={e.id}
                className={isFailure ? "bg-destructive/5" : undefined}
              >
                <TableCell className="text-muted-foreground">
                  {i + 1}
                </TableCell>
                <TableCell className="text-yellow-500 font-mono text-xs">
                  {e.caseNumber || "—"}
                </TableCell>
                <TableCell className="text-muted-foreground text-xs">
                  {e.className}
                </TableCell>
                <TableCell>{e.testName}</TableCell>
                <TableCell className="text-muted-foreground">
                  {e.attempt}
                </TableCell>
                <TableCell>
                  <Badge variant={statusVariant(e.status)} className="text-xs">
                    {e.status}
                  </Badge>
                </TableCell>
                <TableCell className="text-muted-foreground">
                  {e.durationMs}ms
                </TableCell>
                <TableCell>
                  <ErrorDetail
                    errorMessage={e.errorMessage}
                    stackTrace={e.stackTrace}
                    errorType={e.errorType}
                  />
                </TableCell>
                <TableCell>
                  {isFailure ? (
                    <TriageModal
                      projectId={projectId}
                      executionId={e.id}
                      currentStatus={
                        t?.triageStatus || "UNTRIAGED"
                      }
                      currentIssueLink={t?.issueLink || ""}
                      currentComment={t?.comment || ""}
                      onSaved={handleTriageSaved}
                    />
                  ) : null}
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/explorer/TestTable.tsx
git commit -m "feat: refactor TestTable with shadcn/ui Table and Badge components"
```

---

### Task 17: Refactor dashboard page

**Files:**
- Modify: `frontend/src/app/projects/[projectId]/page.tsx`

- [ ] **Step 1: Rewrite dashboard page with Skeleton loading and updated imports**

Write `frontend/src/app/projects/[projectId]/page.tsx`:

```typescript
"use client"

import { useEffect, useState } from "react"
import { useParams } from "next/navigation"
import Navbar from "@/components/layout/Navbar"
import Sidebar from "@/components/layout/Sidebar"
import KpiCards from "@/components/dashboard/KpiCards"
import PassRateChart from "@/components/dashboard/PassRateChart"
import RecentRegressions from "@/components/dashboard/RecentRegressions"
import TriageBreakdown from "@/components/dashboard/TriageBreakdown"
import { api } from "@/lib/api"
import { toast } from "sonner"
import { TrendResponse, FlakyTest, TriageSummary } from "@/lib/types"
import { Skeleton } from "@/components/ui/skeleton"

export default function DashboardPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const id = Number(projectId)
  const [role, setRole] = useState<string>("")
  const [trends, setTrends] = useState<TrendResponse[]>([])
  const [flaky, setFlaky] = useState<FlakyTest[]>([])
  const [triageSummary, setTriageSummary] = useState<TriageSummary | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.auth
      .getRole(id)
      .then(r => setRole(r.role))
      .catch(() => {})
  }, [id])

  useEffect(() => {
    setLoading(true)
    Promise.all([
      api.analysis.getTrends(id),
      api.analysis.getFlakyTests(id),
      api.triage.getSummary(id),
    ])
      .then(([t, f, ts]) => {
        setTrends(t)
        setFlaky(f)
        setTriageSummary(ts)
      })
      .catch(err => {
        toast.error(
          "Failed to load dashboard: " +
            (err instanceof Error ? err.message : "Unknown error")
        )
      })
      .finally(() => setLoading(false))
  }, [id])

  return (
    <div>
      <Navbar projectId={id} />
      <div className="flex">
        <Sidebar projectId={id} role={role} />
        <main className="flex-1 p-6 space-y-6">
          {loading ? (
            <div className="space-y-6">
              <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                <Skeleton className="h-24 rounded-xl" />
                <Skeleton className="h-24 rounded-xl" />
                <Skeleton className="h-24 rounded-xl" />
                <Skeleton className="h-24 rounded-xl" />
              </div>
              <div className="grid grid-cols-2 gap-6">
                <Skeleton className="h-80 rounded-xl" />
                <Skeleton className="h-80 rounded-xl" />
              </div>
              <Skeleton className="h-48 rounded-xl" />
            </div>
          ) : (
            <>
              <KpiCards
                trends={trends}
                flakyCount={flaky.length}
                triageSummary={triageSummary}
              />
              <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                <PassRateChart trends={trends} />
                <RecentRegressions projectId={id} />
              </div>
              <TriageBreakdown summary={triageSummary} />
            </>
          )}
        </main>
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/app/projects/\[projectId\]/page.tsx
git commit -m "feat: refactor dashboard page with Skeleton loading and updated layout"
```

---

### Task 18: Refactor trends page

**Files:**
- Modify: `frontend/src/app/projects/[projectId]/trends/page.tsx`

- [ ] **Step 1: Rewrite trends page with shadcn/ui Table, Skeleton, Card**

Write `frontend/src/app/projects/[projectId]/trends/page.tsx`:

```typescript
"use client"

import { useEffect, useState } from "react"
import { useParams } from "next/navigation"
import Navbar from "@/components/layout/Navbar"
import Sidebar from "@/components/layout/Sidebar"
import PassRateChart from "@/components/dashboard/PassRateChart"
import { api } from "@/lib/api"
import { toast } from "sonner"
import { TrendResponse, FlakyTest, FailureClusterItem } from "@/lib/types"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Skeleton } from "@/components/ui/skeleton"

export default function TrendsPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const id = Number(projectId)
  const [role, setRole] = useState<string>("")
  const [trends, setTrends] = useState<TrendResponse[]>([])
  const [flaky, setFlaky] = useState<FlakyTest[]>([])
  const [clusters, setClusters] = useState<FailureClusterItem[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.auth
      .getRole(id)
      .then(r => setRole(r.role))
      .catch(() => {})
  }, [id])

  useEffect(() => {
    setLoading(true)
    Promise.all([
      api.analysis.getTrends(id),
      api.analysis.getFlakyTests(id),
      api.analysis.getClusters(id),
    ])
      .then(([t, f, c]) => {
        setTrends(t)
        setFlaky(f)
        setClusters(c)
      })
      .catch(err => {
        toast.error(
          "Failed to load trends: " +
            (err instanceof Error ? err.message : "Unknown error")
        )
      })
      .finally(() => setLoading(false))
  }, [id])

  if (loading) {
    return (
      <div>
        <Navbar projectId={id} />
        <div className="flex">
          <Sidebar projectId={id} role={role} />
          <main className="flex-1 p-6 space-y-6">
            <Skeleton className="h-80 rounded-xl" />
            <div className="grid grid-cols-2 gap-6">
              <Skeleton className="h-64 rounded-xl" />
              <Skeleton className="h-64 rounded-xl" />
            </div>
          </main>
        </div>
      </div>
    )
  }

  return (
    <div>
      <Navbar projectId={id} />
      <div className="flex">
        <Sidebar projectId={id} role={role} />
        <main className="flex-1 p-6 space-y-6">
          <PassRateChart trends={trends} />
          <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle className="text-sm text-muted-foreground">
                  FLAKY TESTS
                </CardTitle>
              </CardHeader>
              <CardContent>
                {flaky.length === 0 ? (
                  <p className="text-muted-foreground text-sm">
                    No flaky tests detected
                  </p>
                ) : (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Test</TableHead>
                        <TableHead className="text-right">Score</TableHead>
                        <TableHead className="text-right">
                          Transitions
                        </TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {flaky.map(f => (
                        <TableRow key={f.testCaseKey}>
                          <TableCell>{f.testCaseKey}</TableCell>
                          <TableCell className="text-right text-yellow-500">
                            {(f.flakyScore * 100).toFixed(0)}%
                          </TableCell>
                          <TableCell className="text-right text-muted-foreground">
                            {f.transitionCount}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-sm text-muted-foreground">
                  FAILURE CLUSTERS
                </CardTitle>
              </CardHeader>
              <CardContent>
                {clusters.length === 0 ? (
                  <p className="text-muted-foreground text-sm">No clusters</p>
                ) : (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Error</TableHead>
                        <TableHead className="text-right">Count</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {clusters.map(c => (
                        <TableRow key={c.clusterKey}>
                          <TableCell className="truncate max-w-[300px]">
                            {c.representativeError}
                          </TableCell>
                          <TableCell className="text-right text-muted-foreground">
                            {c.occurrenceCount}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
              </CardContent>
            </Card>
          </div>
        </main>
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/app/projects/\[projectId\]/trends/page.tsx
git commit -m "feat: refactor trends page with shadcn/ui Table, Card, Skeleton"
```

---

### Task 19: Refactor run detail (test explorer) page

**Files:**
- Modify: `frontend/src/app/projects/[projectId]/runs/[runId]/page.tsx`

- [ ] **Step 1: Rewrite run detail page with shadcn/ui Badge, Skeleton**

Write `frontend/src/app/projects/[projectId]/runs/[runId]/page.tsx`:

```typescript
"use client"

import { useEffect, useState } from "react"
import { useParams } from "next/navigation"
import Navbar from "@/components/layout/Navbar"
import Sidebar from "@/components/layout/Sidebar"
import FilterBar from "@/components/explorer/FilterBar"
import TestTable from "@/components/explorer/TestTable"
import { api } from "@/lib/api"
import { toast } from "sonner"
import { TestExecution, TestRun } from "@/lib/types"
import { Badge } from "@/components/ui/badge"
import { Skeleton } from "@/components/ui/skeleton"

export default function TestExplorerPage() {
  const { projectId, runId } = useParams<{
    projectId: string
    runId: string
  }>()
  const id = Number(projectId)
  const [role, setRole] = useState<string>("")
  const [run, setRun] = useState<TestRun | null>(null)
  const [executions, setExecutions] = useState<TestExecution[]>([])
  const [search, setSearch] = useState("")
  const [statusFilter, setStatusFilter] = useState("")
  const [envFilter, setEnvFilter] = useState("")
  const [refreshKey, setRefreshKey] = useState(0)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.auth
      .getRole(id)
      .then(r => setRole(r.role))
      .catch(() => {})
  }, [id])

  useEffect(() => {
    setLoading(true)
    api.collection
      .getRun(id, Number(runId))
      .then((data: any) => {
        setRun(data)
        setExecutions(data.executions || [])
      })
      .catch(err => {
        toast.error(
          "Failed to load test run: " +
            (err instanceof Error ? err.message : "Not found")
        )
      })
      .finally(() => setLoading(false))
  }, [id, runId, refreshKey])

  const filtered = executions.filter(e => {
    if (
      search &&
      !e.testName.toLowerCase().includes(search.toLowerCase()) &&
      !e.className.toLowerCase().includes(search.toLowerCase())
    )
      return false
    if (statusFilter && statusFilter !== "all" && e.status !== statusFilter)
      return false
    return true
  })

  if (loading) {
    return (
      <div>
        <Navbar projectId={id} />
        <div className="flex">
          <Sidebar projectId={id} role={role} />
          <main className="flex-1 p-6 space-y-4">
            <div className="flex gap-3">
              <Skeleton className="h-6 w-24" />
              <Skeleton className="h-6 w-24" />
              <Skeleton className="h-6 w-24" />
              <Skeleton className="h-6 w-16" />
            </div>
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-96 rounded-md" />
          </main>
        </div>
      </div>
    )
  }

  return (
    <div>
      <Navbar projectId={id} />
      <div className="flex">
        <Sidebar projectId={id} role={role} />
        <main className="flex-1 p-6">
          {run && (
            <div className="flex gap-3 mb-4 text-sm flex-wrap items-center">
              <Badge
                variant="default"
                className="bg-green-600 hover:bg-green-600"
              >
                Passed: {run.passed}
              </Badge>
              <Badge variant="destructive">Failed: {run.failed}</Badge>
              <Badge variant="secondary">Skipped: {run.skipped}</Badge>
              <span className="text-muted-foreground">
                Duration: {run.durationMs}ms
              </span>
              <span className="text-muted-foreground">
                Env: {run.environment}
              </span>
              <span className="text-muted-foreground">
                Branch: {run.branch || "—"}
              </span>
            </div>
          )}
          <FilterBar
            search={search}
            onSearchChange={setSearch}
            statusFilter={statusFilter}
            onStatusChange={setStatusFilter}
            envFilter={envFilter}
            onEnvChange={setEnvFilter}
          />
          <TestTable
            projectId={id}
            executions={filtered}
            onTriageSaved={() => {
              setRefreshKey(k => k + 1)
              toast.success("Triage saved")
            }}
          />
        </main>
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/app/projects/\[projectId\]/runs/\[runId\]/page.tsx
git commit -m "feat: refactor run detail page with shadcn/ui Badge and Skeleton"
```

---

### Task 20: Refactor settings page

**Files:**
- Modify: `frontend/src/app/projects/[projectId]/settings/page.tsx`

- [ ] **Step 1: Rewrite settings page with shadcn/ui Card, Table, Select, Button, AlertDialog**

Write `frontend/src/app/projects/[projectId]/settings/page.tsx`:

```typescript
"use client"

import { useEffect, useState, useCallback } from "react"
import { useParams, useRouter } from "next/navigation"
import Navbar from "@/components/layout/Navbar"
import Sidebar from "@/components/layout/Sidebar"
import { api } from "@/lib/api"
import { toast } from "sonner"
import type { ProjectInfo, TeamMember } from "@/lib/types"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Button } from "@/components/ui/button"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { Skeleton } from "@/components/ui/skeleton"

const ROLE_OPTIONS = ["ADMIN", "QA", "VIEWER"] as const

export default function SettingsPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const id = Number(projectId)
  const router = useRouter()

  const [role, setRole] = useState<string>("")
  const [checked, setChecked] = useState(false)
  const [projectInfo, setProjectInfo] = useState<ProjectInfo | null>(null)
  const [members, setMembers] = useState<TeamMember[]>([])
  const [membersLoading, setMembersLoading] = useState(true)
  const [removeTarget, setRemoveTarget] = useState<TeamMember | null>(null)

  const fetchMembers = useCallback(() => {
    api.team
      .listMembers(id)
      .then(setMembers)
      .catch(() => toast.error("Failed to load team members"))
      .finally(() => setMembersLoading(false))
  }, [id])

  useEffect(() => {
    api.auth
      .getRole(id)
      .then(r => {
        setRole(r.role)
        setChecked(true)
        if (r.role !== "ADMIN") {
          toast.error("Only admins can access settings")
          router.push(`/projects/${id}`)
        }
      })
      .catch(() => setChecked(true))

    api.project.get(id).then(setProjectInfo).catch(() => {})
    fetchMembers()
  }, [id])

  const handleRoleChange = (userId: number, newRole: string) => {
    api.team
      .upsertRole(id, userId, newRole)
      .then(updated => {
        setMembers(prev =>
          prev.map(m => (m.userId === userId ? updated : m))
        )
        toast.success(`Role updated to ${newRole}`)
      })
      .catch(err => toast.error(err.message))
  }

  const handleRemoveConfirm = () => {
    if (!removeTarget) return
    api.team
      .removeMember(id, removeTarget.userId)
      .then(() => {
        setMembers(prev =>
          prev.filter(m => m.userId !== removeTarget.userId)
        )
        toast.success(`${removeTarget.username} removed from team`)
      })
      .catch(err => toast.error(err.message))
      .finally(() => setRemoveTarget(null))
  }

  if (!checked || role !== "ADMIN") {
    return (
      <div>
        <Navbar projectId={id} />
        <div className="flex">
          <Sidebar projectId={id} role={role} />
          <main className="flex-1 p-6">
            <div className="flex items-center justify-center h-64 text-muted-foreground">
              Access denied
            </div>
          </main>
        </div>
      </div>
    )
  }

  return (
    <div>
      <Navbar projectId={id} />
      <div className="flex">
        <Sidebar projectId={id} role={role} />
        <main className="flex-1 p-6 space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Project Settings</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">Project Name:</span>
                  <span className="ml-2">{projectInfo?.name || "—"}</span>
                </div>
                <div>
                  <span className="text-muted-foreground">Organization:</span>
                  <span className="ml-2">
                    {projectInfo?.orgName || "—"}
                  </span>
                </div>
                <div>
                  <span className="text-muted-foreground">Team:</span>
                  <span className="ml-2">
                    {projectInfo?.teamName || "—"}
                  </span>
                </div>
                <div>
                  <span className="text-muted-foreground">API Key:</span>
                  <code className="ml-2 text-xs bg-secondary px-2 py-1 rounded">
                    {projectInfo?.apiKey || "—"}
                  </code>
                </div>
                <div>
                  <span className="text-muted-foreground">
                    Flaky Threshold:
                  </span>
                  <span className="ml-2">0.3</span>
                </div>
                <div>
                  <span className="text-muted-foreground">
                    Regression Sigma:
                  </span>
                  <span className="ml-2">2.0</span>
                </div>
                <div>
                  <span className="text-muted-foreground">
                    Analysis Window:
                  </span>
                  <span className="ml-2">30 days</span>
                </div>
              </div>
              <div className="mt-4 p-3 bg-secondary rounded-lg text-xs">
                <span className="text-muted-foreground">
                  Upload endpoint:{" "}
                </span>
                <code className="text-primary">
                  POST /api/v1/projects/{id}/test-results
                </code>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Team Members</CardTitle>
            </CardHeader>
            <CardContent>
              {membersLoading ? (
                <div className="space-y-2">
                  <Skeleton className="h-10 w-full" />
                  <Skeleton className="h-10 w-full" />
                  <Skeleton className="h-10 w-full" />
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>User</TableHead>
                      <TableHead>Role</TableHead>
                      <TableHead className="w-20" />
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {members.map(m => (
                      <TableRow key={m.userId}>
                        <TableCell>
                          <span>{m.username}</span>
                          <span className="text-xs text-muted-foreground ml-2">
                            {m.email}
                          </span>
                        </TableCell>
                        <TableCell>
                          <Select
                            value={m.role}
                            onValueChange={newRole =>
                              handleRoleChange(m.userId, newRole)
                            }
                          >
                            <SelectTrigger className="w-28">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              {ROLE_OPTIONS.map(r => (
                                <SelectItem key={r} value={r}>
                                  {r}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </TableCell>
                        <TableCell>
                          <Button
                            variant="link"
                            size="sm"
                            className="text-destructive h-auto p-0"
                            onClick={() => setRemoveTarget(m)}
                          >
                            Remove
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </main>
      </div>

      <AlertDialog
        open={!!removeTarget}
        onOpenChange={open => !open && setRemoveTarget(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Remove Team Member</AlertDialogTitle>
            <AlertDialogDescription>
              Remove {removeTarget?.username} from the team? This action can be
              undone by re-adding them.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={handleRemoveConfirm}>
              Remove
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/app/projects/\[projectId\]/settings/page.tsx
git commit -m "feat: refactor settings page with shadcn/ui Card, Table, Select, Button, AlertDialog, Skeleton"
```

---

### Task 21: Delete old files and unused imports

**Files:**
- Delete: `frontend/src/components/ui/Card.tsx`
- Delete: `frontend/src/lib/toast.tsx`

- [ ] **Step 1: Remove old custom Card component**

```bash
rm frontend/src/components/ui/Card.tsx
```

- [ ] **Step 2: Remove old custom toast system**

```bash
rm frontend/src/lib/toast.tsx
```

- [ ] **Step 3: Remove tailwind.config.ts (no longer needed with Tailwind CSS 4)**

Tailwind CSS 4 uses CSS-based configuration via `@import "tailwindcss"`. The `tailwind.config.ts` file is only needed if using `@config` directive. Since shadcn/ui works with the CSS-based approach, we can remove the redundant config.

```bash
rm frontend/tailwind.config.ts
```

Also remove `frontend/postcss.config.js` if it references `@tailwindcss/postcss` in a way that's incompatible. Actually, keep it — Tailwind CSS 4 uses `@tailwindcss/postcss` as the PostCSS plugin. The postcss config is fine as-is.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/ui/Card.tsx frontend/src/lib/toast.tsx frontend/tailwind.config.ts
git commit -m "refactor: remove old Card component, toast system, and tailwind config"
```

---

### Task 22: Verify build and type check

**Files:** (none — verification only)

- [ ] **Step 1: Run TypeScript check**

```bash
cd frontend && npx tsc --noEmit
```

Expected: No type errors. If errors appear, fix them inline (likely import path issues from deleted files).

- [ ] **Step 2: Run Next.js build**

```bash
cd frontend && npm run build
```

Expected: Successful production build. May show warnings about unused variables or CSS — address any errors, warnings are OK.

- [ ] **Step 3: Start dev server and smoke test**

```bash
cd frontend && npm run dev
```

Verify in browser at `http://localhost:3000`:
- Login page renders with shadcn/ui Card/Input/Button
- Can navigate to register page
- Backend-dependent pages will show error states (expected if backend isn't running)

- [ ] **Step 4: Commit any final fixes**

```bash
git add -A
git commit -m "chore: final type-check and build fixes"
```
