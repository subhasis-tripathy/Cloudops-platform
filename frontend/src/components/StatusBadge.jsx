const LABELS = { ACTIVE: "Active", COMPLETED: "Completed", ON_HOLD: "On hold" };

export default function StatusBadge({ status }) {
  return <span className={`badge badge-${status}`}>{LABELS[status] || status}</span>;
}
