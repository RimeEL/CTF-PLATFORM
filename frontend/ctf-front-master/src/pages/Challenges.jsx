import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

const DIFF_ORDER = { EASY: 0, MEDIUM: 1, HARD: 2 };

export default function Challenges() {
  const { id: competitionId } = useParams();
  const { user } = useAuth();
  const [challenges, setChallenges] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null);
  const [flag, setFlag] = useState('');
  const [submitMsg, setSubmitMsg] = useState(null);
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ title:'', description:'', category:'mobile', difficulty:'EASY', points:100, initialPoints:100, minimumPoints:50, isActive:true });
  const [filter, setFilter] = useState('ALL');

  const load = () => {
    api.getChallenges(competitionId).then(data => {
      setChallenges(Array.isArray(data) ? data : []);
      setLoading(false);
    });
  };

  useEffect(() => { load(); }, [competitionId]);

  const handleSubmitFlag = async (e) => {
    e.preventDefault();
    setSubmitMsg(null);
    const res = await api.submitFlag(selected.id, flag);
    if (res.correct) setSubmitMsg({ type: 'success', text: '🎯 Correct flag! Points awarded.' });
    else if (res.message?.includes('already')) setSubmitMsg({ type: 'info', text: 'Already solved!' });
    else setSubmitMsg({ type: 'error', text: '✗ Wrong flag. Try again.' });
    setFlag('');
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    const data = await api.createChallenge({ ...form, competitionId });
    if (data.id) { setShowCreate(false); load(); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete challenge?')) return;
    await api.deleteChallenge(id);
    setSelected(null); load();
  };

  const filtered = filter === 'ALL' ? challenges
    : challenges.filter(c => c.difficulty === filter);

  const sorted = [...filtered].sort((a,b) => DIFF_ORDER[a.difficulty] - DIFF_ORDER[b.difficulty]);

  const categories = ['ALL', ...new Set(challenges.map(c => c.difficulty))];

  return (
    <div className="page">
      <div className="section-header">
        <h1 className="section-title">Challenges</h1>
        {user?.role === 'ADMIN' && (
          <button className="btn" onClick={() => setShowCreate(true)}>+ New Challenge</button>
        )}
      </div>

      {/* Filter tabs */}
      <div style={{display:'flex', gap:8, marginBottom:24}}>
        {['ALL','EASY','MEDIUM','HARD'].map(f => (
          <button key={f} className="btn btn-sm"
            style={filter === f ? {background:'var(--accent)', color:'var(--bg)'} : {}}
            onClick={() => setFilter(f)}>{f}</button>
        ))}
        <span style={{marginLeft:'auto', color:'var(--text2)', fontSize:13, fontFamily:'monospace'}}>
          {filtered.length} challenges
        </span>
      </div>

      {loading ? (
        <div className="mono" style={{color:'var(--text2)'}}>Loading challenges...</div>
      ) : sorted.length === 0 ? (
        <div className="empty"><div className="empty-icon">⬡</div><p>No challenges yet</p></div>
      ) : (
        <div className="grid-3">
          {sorted.map(c => (
            <div key={c.id} className="card" style={{cursor:'pointer'}} onClick={() => { setSelected(c); setSubmitMsg(null); setFlag(''); }}>
              <div style={{display:'flex', justifyContent:'space-between', marginBottom:8}}>
                <span className={`tag tag-${c.difficulty?.toLowerCase()}`}>{c.difficulty}</span>
                <span className="mono" style={{color:'var(--accent)', fontWeight:700}}>{c.points} pts</span>
              </div>
              <div style={{fontWeight:700, fontSize:16, marginBottom:4}}>{c.title}</div>
              <div style={{fontSize:12, color:'var(--text2)', marginBottom:8}}>{c.category}</div>
              <div style={{fontSize:13, color:'var(--text)', opacity:0.7, overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap'}}>
                {c.description}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Challenge detail modal */}
      {selected && (
        <div className="modal-overlay" onClick={() => setSelected(null)}>
          <div className="modal" style={{maxWidth:560}} onClick={e => e.stopPropagation()}>
            <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:16}}>
              <div className="modal-title" style={{marginBottom:0}}>{selected.title}</div>
              <button onClick={() => setSelected(null)} style={{background:'none',border:'none',color:'var(--text2)',cursor:'pointer',fontSize:18}}>✕</button>
            </div>

            <div style={{display:'flex', gap:8, marginBottom:16}}>
              <span className={`tag tag-${selected.difficulty?.toLowerCase()}`}>{selected.difficulty}</span>
              <span className="tag" style={{borderColor:'var(--text2)', color:'var(--text2)'}}>{selected.category}</span>
              <span className="mono" style={{marginLeft:'auto', color:'var(--accent)', fontWeight:700}}>{selected.points} pts</span>
            </div>

            <div style={{background:'var(--bg3)', padding:16, marginBottom:16, fontSize:14, lineHeight:1.6, borderLeft:'2px solid var(--border)'}}>
              {selected.description}
            </div>

            <form onSubmit={handleSubmitFlag}>
              {submitMsg && <div className={`alert alert-${submitMsg.type}`}>{submitMsg.text}</div>}
              <div className="input-group">
                <label>Submit Flag</label>
                <input type="text" placeholder="CTF{...}"
                  value={flag} onChange={e => setFlag(e.target.value)}
                  style={{fontFamily:'Share Tech Mono, monospace'}} required />
              </div>
              <div style={{display:'flex', gap:8}}>
                <button type="submit" className="btn btn-success">Submit Flag</button>
                {user?.role === 'ADMIN' && (
                  <button type="button" className="btn btn-danger" onClick={() => handleDelete(selected.id)}>Delete</button>
                )}
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Create challenge modal */}
      {showCreate && (
        <div className="modal-overlay" onClick={() => setShowCreate(false)}>
          <div className="modal" style={{maxWidth:520}} onClick={e => e.stopPropagation()}>
            <div className="modal-title">// New Challenge</div>
            <form onSubmit={handleCreate}>
              <div className="input-group">
                <label>Title</label>
                <input type="text" value={form.title} onChange={e => setForm({...form,title:e.target.value})} required />
              </div>
              <div className="input-group">
                <label>Description</label>
                <textarea rows={3} value={form.description} onChange={e => setForm({...form,description:e.target.value})} style={{resize:'vertical'}} />
              </div>
              <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:12}}>
                <div className="input-group">
                  <label>Category</label>
                  <input type="text" value={form.category} onChange={e => setForm({...form,category:e.target.value})} />
                </div>
                <div className="input-group">
                  <label>Difficulty</label>
                  <select value={form.difficulty} onChange={e => setForm({...form,difficulty:e.target.value})}>
                    <option>EASY</option><option>MEDIUM</option><option>HARD</option>
                  </select>
                </div>
                <div className="input-group">
                  <label>Points</label>
                  <input type="number" value={form.points} onChange={e => setForm({...form,points:+e.target.value})} />
                </div>
                <div className="input-group">
                  <label>Min Points</label>
                  <input type="number" value={form.minimumPoints} onChange={e => setForm({...form,minimumPoints:+e.target.value})} />
                </div>
              </div>
              <div style={{display:'flex', gap:10}}>
                <button type="submit" className="btn btn-success">Create</button>
                <button type="button" className="btn btn-danger" onClick={() => setShowCreate(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
