"use client"

import * as React from "react"
import { Drawer } from "@base-ui/react/drawer"
import { X } from "lucide-react"

import { cn } from "@/lib/utils"

type Side = "top" | "bottom" | "left" | "right"

const sideStyles: Record<Side, string> = {
  top: "inset-x-0 top-0 border-b data-[starting-style]:-translate-y-full data-[ending-style]:-translate-y-full",
  bottom:
    "inset-x-0 bottom-0 border-t data-[starting-style]:translate-y-full data-[ending-style]:translate-y-full",
  left: "inset-y-0 left-0 h-full w-3/4 border-r data-[starting-style]:-translate-x-full data-[ending-style]:-translate-x-full max-w-sm",
  right:
    "inset-y-0 right-0 h-full w-3/4 border-l data-[starting-style]:translate-x-full data-[ending-style]:translate-x-full max-w-sm sm:max-w-sm",
}

interface SheetRootProps extends Omit<React.ComponentProps<typeof Drawer.Root>, "data-side"> {
  side?: Side
}

function SheetRoot({ side = "right", ...props }: SheetRootProps) {
  return (
    <Drawer.Root
      data-slot="sheet-root"
      data-side={side}
      {...props}
    />
  )
}

function SheetTrigger({
  className,
  ...props
}: React.ComponentProps<typeof Drawer.Trigger>) {
  return (
    <Drawer.Trigger
      data-slot="sheet-trigger"
      className={cn(className)}
      {...props}
    />
  )
}

function SheetClose({
  className,
  ...props
}: React.ComponentProps<typeof Drawer.Close>) {
  return (
    <Drawer.Close
      data-slot="sheet-close"
      className={cn("absolute right-4 top-4 rounded-xs opacity-70 ring-offset-background transition-opacity hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:pointer-events-none [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4", className)}
      {...props}
    >
      <X />
      <span className="sr-only">Close</span>
    </Drawer.Close>
  )
}

function SheetContent({
  className,
  children,
  ...props
}: React.ComponentProps<typeof Drawer.Content>) {
  return (
    <Drawer.Portal>
      <Drawer.Backdrop className="data-[ending-style]:outline-none data-[starting-style]:opacity-0 data-[ending-style]:opacity-0 data-[starting-style]:outline-none fixed inset-0 z-50 bg-black/40" />
      <Drawer.Content
        data-slot="sheet-content"
        className={cn(
          "bg-background data-[ending-style]:outline-none fixed z-50 gap-4 p-6 shadow-lg transition-transform duration-200 data-[starting-style]:outline-none",
          className
        )}
        {...props}
      >
        {children}
      </Drawer.Content>
    </Drawer.Portal>
  )
}

function SheetTitle({
  className,
  ...props
}: React.ComponentProps<typeof Drawer.Title>) {
  return (
    <Drawer.Title
      data-slot="sheet-title"
      className={cn("text-lg font-semibold text-foreground", className)}
      {...props}
    />
  )
}

function SheetDescription({
  className,
  ...props
}: React.ComponentProps<typeof Drawer.Description>) {
  return (
    <Drawer.Description
      data-slot="sheet-description"
      className={cn("text-muted-foreground text-sm", className)}
      {...props}
    />
  )
}

export {
  SheetRoot,
  SheetTrigger,
  SheetClose,
  SheetContent,
  SheetTitle,
  SheetDescription,
}

export const Sheet = SheetRoot
