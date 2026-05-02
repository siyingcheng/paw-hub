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
