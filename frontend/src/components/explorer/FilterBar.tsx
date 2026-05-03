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
}

export default function FilterBar({
  search,
  onSearchChange,
  statusFilter,
  onStatusChange,
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
    </div>
  )
}
