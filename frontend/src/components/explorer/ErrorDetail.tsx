'use client';
import { useState } from "react";

interface Props { errorMessage?: string; stackTrace?: string; errorType?: string; }

export default function ErrorDetail({ errorMessage, stackTrace, errorType }: Props) {
  const [open, setOpen] = useState(false);
  if (!errorMessage) return null;

  return (
    <>
      <button onClick={() => setOpen(!open)} className="text-xs text-red-400 hover:underline">{open ? 'Hide' : 'Details'}</button>
      {open && (
        <div className="mt-2 p-3 bg-red-950 border border-red-900 rounded-lg text-xs font-mono">
          {errorType && <div className="text-red-400 mb-1">{errorType}</div>}
          <div className="text-red-300 mb-1">{errorMessage}</div>
          {stackTrace && <pre className="text-gray-400 whitespace-pre-wrap max-h-40 overflow-y-auto">{stackTrace}</pre>}
        </div>
      )}
    </>
  );
}
