import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Auth.css';

export default function Register() {
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      const result = await register(form.username, form.email, form.password);
      if (result.success) {
        setSuccess('Account created! Redirecting to login...');
        setTimeout(() => navigate('/login'), 1500);
      } else {
        setError(result.error || 'Registration failed');
      }
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
          <span style={{color:'var(--accent)'}}>CTF PLATFORM</span>
          <span style={{color:'var(--accent2)'}}> ]</span>
        </div>
        <div className="auth-subtitle mono">&gt; create new account_</div>

        <form onSubmit={handleSubmit}>
          {error && <div className="alert alert-error">{error}</div>}
          {success && <div className="alert alert-success">{success}</div>}
          <div className="input-group">
            <label>Username</label>
            <input type="text" placeholder="choose a username"
              value={form.username}
              onChange={e => setForm({...form, username: e.target.value})}
              required autoFocus />
          </div>
          <div className="input-group">
            <label>Email</label>
            <input type="email" placeholder="your@email.com"
              value={form.email}
              onChange={e => setForm({...form, email: e.target.value})}
              required />
          </div>
          <div className="input-group">
            <label>Password</label>
            <input type="password" placeholder="••••••••"
              value={form.password}
              onChange={e => setForm({...form, password: e.target.value})}
              required />
          </div>
          <button type="submit" className="btn" style={{width:'100%',justifyContent:'center'}} disabled={loading}>
            {loading ? 'Creating account...' : '> Register'}
          </button>
        </form>

        <div className="auth-footer">
          Already have an account? <Link to="/login">Login here</Link>
        </div>
      </div>
    </div>
  );
}
