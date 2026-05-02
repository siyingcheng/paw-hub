"use client"

import * as React from "react"
import { AlertDialog } from "@base-ui/react/alert-dialog"

import { cn } from "@/lib/utils"

function AlertDialogRoot(props: React.ComponentProps<typeof AlertDialog.Root>) {
  return <AlertDialog.Root data-slot="alert-dialog-root" {...props} />
}

function AlertDialogTrigger({
  className,
  ...props
}: React.ComponentProps<typeof AlertDialog.Trigger>) {
  return (
    <AlertDialog.Trigger
      data-slot="alert-dialog-trigger"
      className={cn(className)}
      {...props}
    />
  )
}

function AlertDialogPortal({
  className,
  ...props
}: React.ComponentProps<typeof AlertDialog.Portal>) {
  return (
    <AlertDialog.Portal data-slot="alert-dialog-portal" className={cn(className)} {...props} />
  )
}

function AlertDialogBackdrop({
  className,
  ...props
}: React.ComponentProps<typeof AlertDialog.Backdrop>) {
  return (
    <AlertDialog.Backdrop
      data-slot="alert-dialog-backdrop"
      className={cn(
        "data-[ending-style]:outline-none data-[starting-style]:opacity-0 data-[ending-style]:opacity-0 data-[starting-style]:outline-none fixed inset-0 z-50 bg-black/40",
        className
      )}
      {...props}
    />
  )
}

function AlertDialogPopup({
  className,
  ...props
}: React.ComponentProps<typeof AlertDialog.Popup>) {
  return (
    <AlertDialog.Portal>
      <AlertDialog.Backdrop />
      <AlertDialog.Popup
        data-slot="alert-dialog-popup"
        className={cn(
          "bg-background data-[ending-style]:outline-none data-[starting-style]:translate-x-0 data-[ending-style]:translate-x-0 data-[starting-style]:scale-95 data-[ending-style]:scale-95 data-[starting-style]:opacity-0 data-[ending-style]:opacity-0 fixed left-1/2 top-1/2 z-50 grid w-full max-w-lg -translate-x-1/2 -translate-y-1/2 gap-4 border p-6 shadow-lg duration-200 sm:rounded-xl",
          className
        )}
        {...props}
      />
    </AlertDialog.Portal>
  )
}

function AlertDialogTitle({
  className,
  ...props
}: React.ComponentProps<typeof AlertDialog.Title>) {
  return (
    <AlertDialog.Title
      data-slot="alert-dialog-title"
      className={cn("text-lg font-semibold", className)}
      {...props}
    />
  )
}

function AlertDialogDescription({
  className,
  ...props
}: React.ComponentProps<typeof AlertDialog.Description>) {
  return (
    <AlertDialog.Description
      data-slot="alert-dialog-description"
      className={cn("text-muted-foreground text-sm", className)}
      {...props}
    />
  )
}

function AlertDialogClose({
  className,
  ...props
}: React.ComponentProps<typeof AlertDialog.Close>) {
  return (
    <AlertDialog.Close
      data-slot="alert-dialog-close"
      className={cn(className)}
      {...props}
    />
  )
}

export {
  AlertDialogRoot,
  AlertDialogTrigger,
  AlertDialogPortal,
  AlertDialogBackdrop,
  AlertDialogPopup,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogClose,
}
