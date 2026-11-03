import { useEffect, useState } from "react";
import api, { errorMessage } from "../api";
import StatusBadge from "../components/StatusBadge";
import { formatDate } from "../format";

const ORDER = ["ACTIVE", "COMPLETED", "ON_HOLD"];
const LABELS = { ACTIVE: "Active", COMPLETED: "Completed", ON_HOLD: "On hold" };

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/api/dashboard/stats")
      .then((res) => setStats(res.data))
      .catch((err) => setError(errorMessage(err)));
  }, []);

  if (error) return <p className="error">{error}</p>;
  if (!stats) return <p className="muted">Loading...</p>;

  return (
    <>
      <h1>Dashboard</h1>

      <div className="stat-grid">
        <div className="card stat">
          <span className="muted">Total projects</span>
          <strong>{stats.total}</strong>
        </div>
        {ORDER.map((s) => (
          <div className="card stat" key={s}>
            <span className="muted">{LABELS[s]}</span>
            <strong>{stats.byStatus[s] ?? 0}</strong>
          </div>
        ))}
      </div>

      <div className="card">
        <h2>Projects by status</h2>
        {stats.total === 0 ? (
          <p className="muted">No projects yet. Create one on the Projects page.</p>
        ) : (
          ORDER.map((s) => {
            const count = stats.byStatus[s] ?? 0;
            const pct = (count / stats.total) * 100;
            return (
              <div className="bar-row" key={s}>
                <span className="bar-label">{LABELS[s]}</span>
                <div className="bar-track">
                  <div className={`bar-fill bar-${s}`} style={{ width: `${pct}%` }} />
                </div>
                <span className="bar-count">{count}</span>
              </div>
            );
          })
        )}
      </div>

      <div className="card">
        <h2>Recent projects</h2>
        {stats.recent.length === 0 ? (
          <p className="muted">Nothing here yet.</p>
        ) : (
          <ul className="recent">
            {stats.recent.map((p) => (
              <li key={p.id}>
                <span>{p.name}</span>
                <StatusBadge status={p.status} />
                <span className="muted">{formatDate(p.createdAt)}</span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </>
  );
}
