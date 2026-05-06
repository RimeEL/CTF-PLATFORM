import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export default function Competitions() {
  const { user } = useAuth();
  const [competitions, setCompetitions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ name: '', startTime: '', endTime: '', isActive: true });
  const [msg, setMsg] = useState(null);

  const load = () => {
    api.getCompetitions().then(data => {
      setCompetitions(Array.isArray(data) ? data : []);
      setLoading(false);
    });
  };

  useEffect(() => { load(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    const data = await api.createCompetition({
      ...form,
      startTime: form.startTime || null,
      endTime: form.endTime || null,
    });
    if (data.id) {
      setMsg({ type: 'success', text: 'Competition created!' });
      setShowModal(false);
      load();
    } else {
      setMsg({ type: 'error', text: data.error || 'Failed to create' });
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this competition?')) return;
    await api.deleteCompetition(id);
    load();
  };

  return (
    <div className="page">
      <div className="section-header">
        <h1 className="section-title">Competitions</h1>
        {user?.role === 'ADMIN' && (
          <button className="btn" onClick={() => setShowModal(true)}>+ New Competition</button>
        )}
      </div>

      {msg && <div className={`alert alert-${msg.type}`}>{msg.text}</div>}

      {loading ? (
        <div className="mono" style={{color:'var(--text2)'}}>Loading...</div>
      ) : competitions.length === 0 ? (
        <div className="empty">
          <div className="empty-icon">⬡</div>
          <p>No competitions found</p>
        </div>
      ) : (
        <div className="grid-2">
          {competitions.map(c => (
            <div key={c.id} className="card">
              <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:12}}>
                <div style={{fontWeight:700, fontSize:17}}>{c.name}</div>
                <span className={`tag ${(c.active||c.isActive) ? 'tag-active' : 'tag-inactive'}`}>
                  {(c.active||c.isActive) ? 'Active' : 'Inactive'}
                </span>
              </div>
              <div style={{fontSize:12, color:'var(--text2)', marginBottom:16, fontFamily:'Share Tech Mono, monospace'}}>
                {c.startTime ? `Start: ${new Date(c.startTime).toLocaleString()}` : 'No start time'}
                {c.endTime ? ` | End: ${new Date(c.endTime).toLocaleString()}` : ''}
              </div>
              <div style={{display:'flex', gap:8}}>
                <Link to={`/competitions/${c.id}/challenges`} className="btn btn-sm">
                  Challenges &gt;
                </Link>
                {user?.role === 'ADMIN' && (
                  <button className="btn btn-sm btn-danger" onClick={() => handleDelete(c.id)}>
                    Delete
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-title">// New Competition</div>
            <form onSubmit={handleCreate}>
              <div className="input-group">
                <label>Name</label>
                <input type="text" placeholder="Competition name"
                  value={form.name} onChange={e => setForm({...form, name: e.target.value})} required />
              </div>
              <div className="input-group">
                <label>Start Time</label>
                <input type="datetime-local"
                  value={form.startTime} onChange={e => setForm({...form, startTime: e.target.value})} />
              </div>
              <div className="input-group">
                <label>End Time</label>
                <input type="datetime-local"
                  value={form.endTime} onChange={e => setForm({...form, endTime: e.target.value})} />
              </div>
              <div style={{display:'flex', gap:10}}>
                <button type="submit" className="btn btn-success">Create</button>
                <button type="button" className="btn btn-danger" onClick={() => setShowModal(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
