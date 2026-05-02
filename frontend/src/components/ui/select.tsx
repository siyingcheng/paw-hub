"use client"

import * as React from "react"
import { Select } from "@base-ui/react/select"
import { ChevronDown, Check } from "lucide-react"

import { cn } from "@/lib/utils"

function SelectRoot(props: React.ComponentProps<typeof Select.Root>) {
  return <Select.Root data-slot="select-root" {...props} />
}

function SelectTrigger({
  className,
  children,
  ...props
}: React.ComponentProps<typeof Select.Trigger>) {
  return (
    <Select.Trigger
      data-slot="select-trigger"
      className={cn(
        "flex h-8 w-full items-center justify-between gap-2 rounded-lg border border-input bg-transparent px-3 py-1 text-sm whitespace-nowrap shadow-xs transition-colors outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20 dark:bg-input/30 [&>span]:truncate [&_svg]:shrink-0",
        className
      )}
      {...props}
    >
      {children}
      <Select.Icon className="pointer-events-none shrink-0 opacity-50">
        <ChevronDown className="size-4" />
      </Select.Icon>
    </Select.Trigger>
  )
}

function SelectValue({
  className,
  ...props
}: React.ComponentProps<typeof Select.Value>) {
  return (
    <Select.Value
      data-slot="select-value"
      className={cn("text-foreground", className)}
      {...props}
    />
  )
}

function SelectPopup({
  className,
  ...props
}: React.ComponentProps<typeof Select.Popup>) {
  return (
    <Select.Portal>
      <Select.Positioner className="outline-none">
        <Select.Popup
          data-slot="select-popup"
          className={cn(
            "bg-popover text-popover-foreground relative z-50 max-h-96 min-w-32 overflow-hidden rounded-lg border shadow-md shadow-black/5 data-[side=bottom]:animate-in data-[side=left]:animate-in data-[side=right]:animate-in data-[side=top]:animate-in",
            className
          )}
          {...props}
        />
      </Select.Positioner>
    </Select.Portal>
  )
}

function SelectItem({
  className,
  children,
  ...props
}: React.ComponentProps<typeof Select.Item>) {
  return (
    <Select.Item
      data-slot="select-item"
      className={cn(
        "relative flex w-full cursor-default items-center gap-2 rounded-sm py-1.5 pr-8 pl-2 text-sm outline-none select-none data-[highlighted]:bg-accent data-[highlighted]:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
        className
      )}
      {...props}
    >
      <Select.ItemIndicator className="absolute right-2 flex items-center justify-center">
        <Check className="size-4" />
      </Select.ItemIndicator>
      <Select.ItemText>{children}</Select.ItemText>
    </Select.Item>
  )
}

function SelectGroup({
  className,
  ...props
}: React.ComponentProps<typeof Select.Group>) {
  return (
    <Select.Group
      data-slot="select-group"
      className={cn("", className)}
      {...props}
    />
  )
}

function SelectLabel({
  className,
  ...props
}: React.ComponentProps<typeof Select.Label>) {
  return (
    <Select.Label
      data-slot="select-label"
      className={cn("px-2 py-1.5 text-sm font-semibold", className)}
      {...props}
    />
  )
}

export {
  SelectRoot,
  SelectTrigger,
  SelectValue,
  SelectPopup,
  SelectItem,
  SelectGroup,
  SelectLabel,
}
