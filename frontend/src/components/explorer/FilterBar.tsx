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
      <Select value={statusFilter} onValueChange={onStatusChange}>
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
