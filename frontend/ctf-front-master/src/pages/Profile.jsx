import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export default function Profile() {
  const { user, login } = useAuth();
  const [form, setForm] = useState({ username: user?.username || '', email: user?.email || '' });
  const [msg, setMsg] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleUpdate = async (e) => {
    e.preventDefault();
    setLoading(true); setMsg(null);
    const data = await api.updateMe(form);
    if (data.id) setMsg({ type: 'success', text: 'Profile updated successfully!' });
    else setMsg({ type: 'error', text: data.error || 'Update failed' });
    setLoading(false);
  };

  return (
    <div className="page" style={{maxWidth:600}}>
      <div className="section-header">
        <h1 className="section-title">Profile</h1>
      </div>

      <div className="card" style={{marginBottom:24}}>
        <div style={{display:'flex', alignItems:'center', gap:20, marginBottom:20}}>
          <div style={{
            width:64, height:64,
            background:'var(--accent)',
            color:'var(--bg)',
            display:'flex', alignItems:'center', justifyContent:'center',
            fontSize:28, fontWeight:700,
            clipPath:'polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%)',
          }}>
            {user?.username?.[0]?.toUpperCase()}
          </div>
          <div>
            <div style={{fontSize:22, fontWeight:700}}>{user?.username}</div>
            <div style={{color:'var(--accent2)', fontSize:12, letterSpacing:2, textTransform:'uppercase'}}>{user?.role}</div>
            <div style={{color:'var(--text2)', fontSize:12, fontFamily:'monospace', marginTop:4}}>{user?.email}</div>
          </div>
        </div>

        <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:12}}>
          <div style={{background:'var(--bg2)', padding:12, border:'1px solid var(--border)'}}>
            <div style={{fontSize:11, color:'var(--text2)', letterSpacing:2, textTransform:'uppercase', marginBottom:4}}>User ID</div>
            <div className="mono" style={{fontSize:11, color:'var(--text)', wordBreak:'break-all'}}>{user?.id}</div>
          </div>
          <div style={{background:'var(--bg2)', padding:12, border:'1px solid var(--border)'}}>
            <div style={{fontSize:11, color:'var(--text2)', letterSpacing:2, textTransform:'uppercase', marginBottom:4}}>Status</div>
            <div style={{color:'var(--accent3)', fontWeight:600}}>● Active</div>
          </div>
        </div>
      </div>

      <div className="card">
        <div style={{fontWeight:700, fontSize:16, letterSpacing:2, textTransform:'uppercase', color:'var(--accent)', marginBottom:20}}>
          // Edit Profile
        </div>
        {msg && <div className={`alert alert-${msg.type}`}>{msg.text}</div>}
        <form onSubmit={handleUpdate}>
          <div className="input-group">
            <label>Username</label>
            <input type="text" value={form.username}
              onChange={e => setForm({...form, username: e.target.value})} required />
          </div>
          <div className="input-group">
            <label>Email</label>
            <input type="email" value={form.email}
              onChange={e => setForm({...form, email: e.target.value})} required />
          </div>
          <button type="submit" className="btn btn-success" disabled={loading}>
            {loading ? 'Saving...' : '> Save Changes'}
          </button>
        </form>
      </div>
    </div>
  );
}
