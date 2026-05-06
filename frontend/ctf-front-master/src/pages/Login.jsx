import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Auth.css';

export default function Login() {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      const result = await login(form.username, form.password);
      if (result.success) navigate('/');
      else setError(result.error || 'Authentication failed');
    } catch (err) {
      console.error(err);
      setError('An unexpected error occurred. Please check if the server is running.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-bg" />
      <div className="auth-box">
        <div className="auth-logo">
          <span style={{color:'var(--accent2)'}}>[ </span>
          <span className="glitch" style={{color:'var(--accent)'}}>CTF PLATFORM</span>
          <span style={{color:'var(--accent2)'}}> ]</span>
        </div>
        <div className="auth-subtitle mono">&gt; authenticate to continue_</div>

        <form onSubmit={handleSubmit}>
          {error && <div className="alert alert-error">{error}</div>}
          <div className="input-group">
            <label>Username</label>
            <input
              type="text" placeholder="enter username"
              value={form.username}
              onChange={e => setForm({...form, username: e.target.value})}
              required autoFocus
            />
          </div>
          <div className="input-group">
            <label>Password</label>
            <input
              type="password" placeholder="••••••••"
              value={form.password}
              onChange={e => setForm({...form, password: e.target.value})}
              required
            />
          </div>
          <button type="submit" className="btn" style={{width:'100%', justifyContent:'center'}} disabled={loading}>
            {loading ? 'Authenticating...' : '> Login'}
          </button>
        </form>

        <div className="auth-footer">
          No account? <Link to="/register">Register here</Link>
        </div>
      </div>
    </div>
  );
}
