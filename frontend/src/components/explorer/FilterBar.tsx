'use client';

interface Props {
  search: string;
  onSearchChange: (v: string) => void;
  statusFilter: string;
  onStatusChange: (v: string) => void;
  envFilter: string;
  onEnvChange: (v: string) => void;
}

export default function FilterBar({ search, onSearchChange, statusFilter, onStatusChange, envFilter, onEnvChange }: Props) {
  return (
    <div className="flex gap-3 mb-4 flex-wrap">
      <input
        className="px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm flex-1 min-w-[200px]"
        placeholder="Search test name..."
        value={search}
        onChange={e => onSearchChange(e.target.value)}
      />
      <select className="px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm" value={statusFilter} onChange={e => onStatusChange(e.target.value)}>
        <option value="">All statuses</option>
        <option value="FAIL">FAIL</option>
        <option value="PASS">PASS</option>
        <option value="SKIP">SKIP</option>
        <option value="ERROR">ERROR</option>
      </select>
      <select className="px-3 py-2 rounded-lg bg-gray-800 border border-gray-700 text-sm" value={envFilter} onChange={e => onEnvChange(e.target.value)}>
        <option value="">All envs</option>
        <option value="dev">dev</option>
        <option value="staging">staging</option>
        <option value="prod">prod</option>
      </select>
    </div>
  );
}
