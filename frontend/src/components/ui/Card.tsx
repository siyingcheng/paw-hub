export default function Card({ children, className = "" }: { children: React.ReactNode; className?: string }) {
  return <div className={`bg-gray-900 rounded-xl border border-gray-800 p-4 ${className}`}>{children}</div>;
}
