import { useEffect, useState } from "react";
import api, { errorMessage } from "../api";
import { useAuth } from "../AuthContext";
import StatusBadge from "../components/StatusBadge";
import { formatDate } from "../format";

const EMPTY = { name: "", description: "", status: "ACTIVE" };
const STATUSES = ["ACTIVE", "COMPLETED", "ON_HOLD"];

export default function Projects() {
  const { user } = useAuth();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [form, setForm] = useState(EMPTY);
  const [editingId, setEditingId] = useState(null);
  const [filter, setFilter] = useState("ALL");
  const [saving, setSaving] = useState(false);

  const load = async () => {
    try {
      const { data } = await api.get("/api/projects");
      setProjects(data);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      if (editingId) await api.put(`/api/projects/${editingId}`, form);
      else await api.post("/api/projects", form);
      setForm(EMPTY);
      setEditingId(null);
      await load();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const startEdit = (p) => {
    setEditingId(p.id);
    setForm({ name: p.name, description: p.description || "", status: p.status });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const cancelEdit = () => {
    setEditingId(null);
    setForm(EMPTY);
  };

  const remove = async (p) => {
    if (!window.confirm(`Delete "${p.name}"?`)) return;
    try {
      setError("");
      await api.delete(`/api/projects/${p.id}`);
      if (editingId === p.id) cancelEdit();
      await load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const visible = filter === "ALL" ? projects : projects.filter((p) => p.status === filter);
  const isAdmin = user?.role === "ADMIN";

  return (
    <>
      <h1>Projects</h1>

      <form className="card" onSubmit={submit}>
        <h2>{editingId ? "Edit project" : "New project"}</h2>
        {error && <p className="error">{error}</p>}
        <label>Name
          <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        </label>
        <label>Description
          <textarea rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </label>
        <label>Status
          <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
            {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </label>
        <div className="row">
          <button className="btn" disabled={saving}>{saving ? "Saving..." : editingId ? "Update" : "Create"}</button>
          {editingId && <button type="button" className="btn btn-ghost" onClick={cancelEdit}>Cancel</button>}
        </div>
      </form>

      <div className="card">
        <div className="row between">
          <h2>{isAdmin ? "All projects" : "Your projects"}</h2>
          <select value={filter} onChange={(e) => setFilter(e.target.value)}>
            <option value="ALL">All statuses</option>
            {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>

        {loading ? (
          <p className="muted">Loading...</p>
        ) : visible.length === 0 ? (
          <p className="muted">No projects to show.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Status</th>
                  {isAdmin && <th>Owner</th>}
                  <th>Created</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {visible.map((p) => (
                  <tr key={p.id}>
                    <td>
                      <strong>{p.name}</strong>
                      {p.description && <div className="muted small">{p.description}</div>}
                    </td>
                    <td><StatusBadge status={p.status} /></td>
                    {isAdmin && <td className="small">{p.ownerEmail}</td>}
                    <td className="small">{formatDate(p.createdAt)}</td>
                    <td className="actions">
                      <button className="btn btn-ghost" onClick={() => startEdit(p)}>Edit</button>
                      <button className="btn btn-danger" onClick={() => remove(p)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  );
}
