import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export default function Scoreboard() {
  const { user } = useAuth();
  const [scores, setScores] = useState([]);
  const [type, setType] = useState('user');
  const [loading, setLoading] = useState(true);

  const load = (t) => {
    setLoading(true);
    api.getScoreboard(t).then(data => {
      setScores(Array.isArray(data) ? data : []);
      setLoading(false);
    });
  };

  useEffect(() => { load(type); }, [type]);

  const medals = ['🥇', '🥈', '🥉'];

  return (
    <div className="page">
      <div className="section-header">
        <h1 className="section-title">Scoreboard</h1>
        <div style={{display:'flex', gap:8}}>
          <button className="btn btn-sm"
            style={type==='user' ? {background:'var(--accent)',color:'var(--bg)'} : {}}
            onClick={() => setType('user')}>Players</button>
          <button className="btn btn-sm"
            style={type==='team' ? {background:'var(--accent)',color:'var(--bg)'} : {}}
            onClick={() => setType('team')}>Teams</button>
        </div>
      </div>

      {/* Top 3 podium */}
      {!loading && scores.length >= 3 && (
        <div style={{display:'flex', justifyContent:'center', gap:16, marginBottom:32, alignItems:'flex-end'}}>
          {[scores[1], scores[0], scores[2]].map((s, i) => {
            const heights = ['100px', '130px', '80px'];
            const actualRank = i === 0 ? 2 : i === 1 ? 1 : 3;
            return (
              <div key={s?.userId || s?.teamId} style={{textAlign:'center', width:120}}>
                <div style={{fontSize:24, marginBottom:4}}>{medals[actualRank-1]}</div>
                <div style={{fontWeight:700, fontSize:14, marginBottom:4, color: actualRank===1 ? 'var(--accent)' : 'var(--text)'}}>
                  {s?.username || s?.teamName}
                </div>
                <div className="mono" style={{color:'var(--accent)', fontSize:13, marginBottom:6}}>{s?.totalPoints} pts</div>
                <div style={{
                  height: heights[i],
                  background: actualRank===1 ? 'rgba(0,212,255,0.15)' : 'rgba(0,212,255,0.05)',
                  border: `1px solid ${actualRank===1 ? 'var(--accent)' : 'var(--border)'}`,
                  display:'flex', alignItems:'center', justifyContent:'center',
                  fontSize:24, fontWeight:700, fontFamily:'monospace',
                  color: actualRank===1 ? 'var(--accent)' : 'var(--text2)',
                }}>#{actualRank}</div>
              </div>
            );
          })}
        </div>
      )}

      {loading ? (
        <div className="mono" style={{color:'var(--text2)'}}>Loading scoreboard...</div>
      ) : scores.length === 0 ? (
        <div className="empty"><div className="empty-icon">⬡</div><p>No scores yet</p></div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Rank</th>
                <th>{type === 'user' ? 'Player' : 'Team'}</th>
                <th>Points</th>
              </tr>
            </thead>
            <tbody>
              {scores.map((s, i) => {
                const isMe = s.username === user?.username;
                return (
                  <tr key={s.userId || s.teamId} style={isMe ? {background:'rgba(0,212,255,0.05)'} : {}}>
                    <td className="mono" style={{
                      color: i===0 ? '#ffd700' : i===1 ? '#c0c0c0' : i===2 ? '#cd7f32' : 'var(--text2)',
                      fontWeight: i < 3 ? 700 : 400,
                    }}>
                      {i < 3 ? medals[i] : `#${s.rank}`}
                    </td>
                    <td style={{fontWeight:600, color: isMe ? 'var(--accent)' : 'var(--text)'}}>
                      {s.username || s.teamName}
                      {isMe && <span style={{marginLeft:8, fontSize:11, color:'var(--accent2)'}}>[ YOU ]</span>}
                    </td>
                    <td className="mono" style={{color:'var(--accent)', fontWeight:700}}>{s.totalPoints}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
