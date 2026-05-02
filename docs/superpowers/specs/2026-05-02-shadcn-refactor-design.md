# shadcn/ui Frontend Refactor — Design Spec

**Date:** 2026-05-02
**Branch:** `refactor/light-weight-v1`

## Summary

Replace all custom Tailwind components in the Paw-Hub frontend with shadcn/ui equivalents. Add theme switching (OS/Dark/Light), a project switcher dropdown, mobile-responsive sidebar, and proper accessible dialogs. All existing API contracts, data flow, and charting (Recharts) remain unchanged.

## Architecture

**Stack (unchanged):** Next.js 15 App Router, React 19, Tailwind CSS 4, `output: 'standalone'`

**New dependencies:**
- `shadcn/ui` components (installed via `npx shadcn@latest add`)
- `next-themes` — theme persistence and OS detection
- `sonner` — toast notifications (replaces custom Context-based system)
- `lucide-react` — icons (comes with shadcn/ui)
- `class-variance-authority`, `clsx`, `tailwind-merge` — shadcn/ui utilities
- `tailwindcss-animate` — animation plugin

**Removed dependencies:** none (existing deps stay)

**File structure after refactor:**
```
src/
├── app/
│   ├── globals.css              (rewritten: CSS vars + dark theme tokens)
│   ├── layout.tsx               (add ThemeProvider + Toaster)
│   ├── page.tsx                 (unchanged)
│   ├── login/page.tsx           (Button, Input, Card)
│   ├── register/page.tsx        (Button, Input, Card)
│   └── projects/[projectId]/
│       ├── page.tsx             (Skeleton, Card, Badge)
│       ├── trends/page.tsx      (Skeleton, Table, Card)
│       ├── runs/[runId]/page.tsx (Badge, Skeleton)
│       └── settings/page.tsx    (Card, Table, Select, Button, AlertDialog)
├── components/
│   ├── layout/
│   │   ├── Navbar.tsx           (Button, DropdownMenu, theme toggle, project switcher)
│   │   └── Sidebar.tsx          (Button ghost, Sheet for mobile)
│   ├── dashboard/
│   │   ├── KpiCards.tsx         (Card)
│   │   ├── PassRateChart.tsx    (Card wrapper, Recharts unchanged)
│   │   ├── RecentRegressions.tsx (Card, Alert, Badge)
│   │   └── TriageBreakdown.tsx  (Card, Table)
│   ├── explorer/
│   │   ├── FilterBar.tsx        (Input, Select)
│   │   ├── TestTable.tsx        (Table, Badge)
│   │   ├── ErrorDetail.tsx      (Alert, collapsible)
│   │   └── TriageModal.tsx      (Dialog, Select, Input, Textarea, Button)
│   ├── ui/                      (shadcn/ui generated components)
│   ├── theme-provider.tsx       (next-themes wrapper)
│   └── theme-toggle.tsx         (DropdownMenu toggle)
└── lib/
    ├── api.ts                   (unchanged)
    ├── auth.ts                  (unchanged)
    ├── types.ts                 (unchanged)
    └── utils.ts                 (new: cn() helper)
```

**Deleted:** `components/ui/Card.tsx`, `lib/toast.tsx`

## Component Mapping

| Current | shadcn/ui | Where used |
|---|---|---|
| Raw `<button>` | `Button` (default, outline, ghost, destructive) | Login, Register, Navbar, Sidebar, TriageModal, Settings |
| Raw `<input>` | `Input` | Login, Register, FilterBar, TriageModal |
| Raw `<select>` | `Select` (Radix dropdown) | FilterBar, TriageModal, Settings role picker |
| Custom `Card.tsx` | `Card, CardHeader, CardContent` | KPIs, Charts, Regressions, Triage, Settings |
| Inline modal `<div>` | `Dialog` (focus trap, ESC close, backdrop) | TriageModal |
| Raw `<table>` | `Table, TableHeader, TableBody, TableRow, TableCell` | TestTable, Trends, Settings members |
| Inline status spans | `Badge` (default, secondary, destructive, outline) | TestTable status, KPI counts |
| Custom toast Context | `sonner` (`toast.success()`, `toast.error()`) | All pages |
| Loading text strings | `Skeleton` | Dashboard, Trends, Run detail |
| `window.confirm()` | `AlertDialog` | Settings member removal |

## Data Flow (unchanged)

- Pages fetch via `api.*` methods → `useState` → props to components
- `api.ts` reads JWT from `localStorage`, attaches Bearer header, unwraps `ApiResponse.data`
- `auth.ts` — `getToken()`, `setToken()`, `clearAuth()`, `isAuthenticated()`

## New Features

### Theme Switching
- `ThemeProvider` (next-themes) in `layout.tsx` wraps all children
- Persists to `localStorage` key `"theme"`, falls back to OS preference
- `ThemeToggle` component in Navbar: `DropdownMenu` with Light / Dark / System
- CSS variables use `dark` class on `<html>` to toggle palette
- Hybrid theme: dark atmosphere with blue accents, using shadcn/ui semantic tokens

### Project Switcher
- Navbar loads project list on mount
- `DropdownMenu` lists projects, click navigates to `/projects/{id}`
- Login redirects to first available project (removes hardcoded `/projects/1`)

### Mobile Responsive Sidebar
- Desktop: same fixed sidebar behavior
- Mobile (<768px): hamburger `Button` in Navbar opens `Sheet` (slide-over drawer)
- Sheet contains same navigation links + role badge

### Accessible Triage Modal
- Replace inline `<div>` overlay with `Dialog` (Radix)
- Proper focus trap, ESC to close, click-outside-to-close
- Same fields: status Select, issue link Input, comment Textarea, Save/Cancel Buttons

## Error Handling & Edge Cases

- All existing try/catch patterns preserved — error toasts use sonner instead of Context
- Empty states remain: "No trend data", "No regressions detected", "No flaky tests" — wrapped in Card
- Auth expiration: `api.ts` throws on `success: false`, pages catch and toast
- Form loading: Button `loading` prop shows spinner, disables interaction
- Member removal: `AlertDialog` replaces `window.confirm()` with accessible confirmation

## Verification

1. Start backend: `cd backend && ./mvnw spring-boot:run`
2. Start frontend: `cd frontend && npm run dev`
3. Manual checklist:
   - Login/register flows work
   - Dashboard loads: KPIs, pass rate chart, regressions, triage breakdown
   - Trends page loads: chart, flaky tests table, failure clusters table
   - Run detail page: test table filters, triage modal opens/saves
   - Settings page: project info, team member list, role changes, member removal
   - Theme toggle: OS/Dark/Light cycles, persists on reload
   - Mobile: sidebar collapses to Sheet below 768px
