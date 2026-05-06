import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export default function Dashboard() {
  const { user } = useAuth();
  const [competitions, setCompetitions] = useState([]);
  const [scoreboard, setScoreboard] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.getCompetitions(),
      api.getScoreboard(),
    ]).then(([comps, score]) => {
      setCompetitions(Array.isArray(comps) ? comps : []);
      setScoreboard(Array.isArray(score) ? score.slice(0, 5) : []);
    }).finally(() => setLoading(false));
  }, []);

  const activeComps = competitions.filter(c => c.active || c.isActive);

  return (
    <div className="page">
      <div style={{marginBottom: 32}}>
        <h1 style={{fontSize: 28, fontWeight: 700, letterSpacing: 3, color: 'var(--accent)', marginBottom: 4}}>
          <span style={{color:'var(--text2)'}}>// </span>
          <span className="glitch">DASHBOARD</span>
        </h1>
        <div className="mono" style={{color:'var(--text2)', fontSize:13}}>
          &gt; Welcome back, <span style={{color:'var(--accent)'}}>{user?.username}</span>
        </div>
      </div>

      {/* Stats */}
      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-value">{competitions.length}</div>
          <div className="stat-label">Competitions</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{activeComps.length}</div>
          <div className="stat-label">Active Now</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{scoreboard.length}</div>
          <div className="stat-label">Top Players</div>
        </div>
        <div className="stat-card">
          <div className="stat-value" style={{color:'var(--accent2)'}}>
            {scoreboard.find(s => s.username === user?.username)?.totalPoints ?? 0}
          </div>
          <div className="stat-label">Your Points</div>
        </div>
      </div>

      <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:24}}>
        {/* Active Competitions */}
        <div>
          <div className="section-header">
            <div className="section-title">Active Competitions</div>
            <Link to="/competitions" className="btn btn-sm">View All</Link>
          </div>
          {loading ? <div className="mono" style={{color:'var(--text2)'}}>Loading...</div> :
            activeComps.length === 0 ? (
              <div className="empty"><div className="empty-icon">⬡</div><p>No active competitions</p></div>
            ) : (
              <div style={{display:'flex', flexDirection:'column', gap:12}}>
                {activeComps.slice(0,4).map(c => (
                  <div key={c.id} className="card">
                    <div style={{fontWeight:700, marginBottom:4}}>{c.name}</div>
                    <div style={{fontSize:12, color:'var(--text2)', marginBottom:10}}>
                      {c.startTime ? new Date(c.startTime).toLocaleDateString() : 'Ongoing'}
                    </div>
                    <Link to={`/competitions/${c.id}/challenges`} className="btn btn-sm">
                      Enter &gt;
                    </Link>
                  </div>
                ))}
              </div>
            )
          }
        </div>

        {/* Top Scoreboard */}
        <div>
          <div className="section-header">
            <div className="section-title">Top Players</div>
            <Link to="/scoreboard" className="btn btn-sm">Full Board</Link>
          </div>
          {loading ? <div className="mono" style={{color:'var(--text2)'}}>Loading...</div> :
            scoreboard.length === 0 ? (
              <div className="empty"><div className="empty-icon">⬡</div><p>No scores yet</p></div>
            ) : (
              <div className="table-wrap">
                <table>
                  <thead><tr><th>#</th><th>Player</th><th>Points</th></tr></thead>
                  <tbody>
                    {scoreboard.map((s, i) => (
                      <tr key={s.userId} style={s.username === user?.username ? {color:'var(--accent)'} : {}}>
                        <td className="mono" style={{color: i === 0 ? '#ffd700' : i === 1 ? '#c0c0c0' : i === 2 ? '#cd7f32' : 'var(--text2)'}}>
                          #{s.rank}
                        </td>
                        <td style={{fontWeight:600}}>{s.username}</td>
                        <td className="mono" style={{color:'var(--accent)'}}>{s.totalPoints}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          }
        </div>
      </div>
    </div>
  );
}
