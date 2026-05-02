"use client"

import * as React from "react"
import { Dialog } from "@base-ui/react/dialog"
import { X } from "lucide-react"

import { cn } from "@/lib/utils"

function DialogRoot(props: React.ComponentProps<typeof Dialog.Root>) {
  return <Dialog.Root data-slot="dialog-root" {...props} />
}

function DialogTrigger({
  className,
  ...props
}: React.ComponentProps<typeof Dialog.Trigger>) {
  return (
    <Dialog.Trigger
      data-slot="dialog-trigger"
      className={cn(className)}
      {...props}
    />
  )
}

function DialogPortal({
  className,
  ...props
}: React.ComponentProps<typeof Dialog.Portal>) {
  return (
    <Dialog.Portal data-slot="dialog-portal" className={cn(className)} {...props} />
  )
}

function DialogBackdrop({
  className,
  ...props
}: React.ComponentProps<typeof Dialog.Backdrop>) {
  return (
    <Dialog.Backdrop
      data-slot="dialog-backdrop"
      className={cn(
        "data-[ending-style]:outline-none data-[starting-style]:opacity-0 data-[ending-style]:opacity-0 data-[starting-style]:outline-none fixed inset-0 z-50 bg-black/40",
        className
      )}
      {...props}
    />
  )
}

function DialogPopup({
  className,
  ...props
}: React.ComponentProps<typeof Dialog.Popup>) {
  return (
    <Dialog.Portal>
      <Dialog.Backdrop />
      <Dialog.Popup
        data-slot="dialog-popup"
        className={cn(
          "bg-background data-[ending-style]:outline-none data-[starting-style]:translate-x-0 data-[ending-style]:translate-x-0 data-[starting-style]:scale-95 data-[ending-style]:scale-95 data-[starting-style]:opacity-0 data-[ending-style]:opacity-0 fixed left-1/2 top-1/2 z-50 grid w-full max-w-lg -translate-x-1/2 -translate-y-1/2 gap-4 border p-6 shadow-lg duration-200 sm:rounded-xl",
          className
        )}
        {...props}
      />
    </Dialog.Portal>
  )
}

function DialogClose({
  className,
  ...props
}: React.ComponentProps<typeof Dialog.Close>) {
  return (
    <Dialog.Close
      data-slot="dialog-close"
      className={cn(
        "data-[slot=dialog-close]:absolute data-[slot=dialog-close]:right-4 data-[slot=dialog-close]:top-4 data-[slot=dialog-close]:rounded-xs data-[slot=dialog-close]:opacity-70 data-[slot=dialog-close]:ring-offset-background data-[slot=dialog-close]:transition-opacity hover:data-[slot=dialog-close]:opacity-100 focus:data-[slot=dialog-close]:outline-none focus:data-[slot=dialog-close]:ring-2 focus:data-[slot=dialog-close]:ring-ring focus:data-[slot=dialog-close]:ring-offset-2 disabled:data-[slot=dialog-close]:pointer-events-none [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
        className
      )}
      {...props}
    >
      <X />
      <span className="sr-only">Close</span>
    </Dialog.Close>
  )
}

function DialogTitle({
  className,
  ...props
}: React.ComponentProps<typeof Dialog.Title>) {
  return (
    <Dialog.Title
      data-slot="dialog-title"
      className={cn("text-lg font-semibold leading-none tracking-tight", className)}
      {...props}
    />
  )
}

function DialogDescription({
  className,
  ...props
}: React.ComponentProps<typeof Dialog.Description>) {
  return (
    <Dialog.Description
      data-slot="dialog-description"
      className={cn("text-muted-foreground text-sm", className)}
      {...props}
    />
  )
}

export {
  DialogRoot,
  DialogTrigger,
  DialogPortal,
  DialogBackdrop,
  DialogPopup,
  DialogClose,
  DialogTitle,
  DialogDescription,
}
