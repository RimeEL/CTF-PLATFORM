import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Layout.css';

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-logo">
          <span className="logo-bracket">[</span>
          <span className="logo-text">CTF</span>
          <span className="logo-bracket">]</span>
        </div>

        <div className="sidebar-user">
          <div className="user-avatar">{user?.username?.[0]?.toUpperCase()}</div>
          <div>
            <div className="user-name">{user?.username}</div>
            <div className="user-role">{user?.role}</div>
          </div>
        </div>

        <nav className="sidebar-nav">
          <NavLink to="/" end className={({isActive}) => isActive ? 'nav-item active' : 'nav-item'}>
            <span className="nav-icon">⬡</span> Dashboard
          </NavLink>
          <NavLink to="/competitions" className={({isActive}) => isActive ? 'nav-item active' : 'nav-item'}>
            <span className="nav-icon">⬡</span> Competitions
          </NavLink>
          <NavLink to="/scoreboard" className={({isActive}) => isActive ? 'nav-item active' : 'nav-item'}>
            <span className="nav-icon">⬡</span> Scoreboard
          </NavLink>
          <NavLink to="/profile" className={({isActive}) => isActive ? 'nav-item active' : 'nav-item'}>
            <span className="nav-icon">⬡</span> Profile
          </NavLink>
        </nav>

        <div className="sidebar-footer">
          <div className="status-dot" /> <span>System Online</span>
          <button className="logout-btn" onClick={handleLogout}>[ LOGOUT ]</button>
        </div>
      </aside>

      <main className="main-content">
        <div className="top-bar">
          <div className="mono top-bar-text">
            &gt; logged in as <span style={{color:'var(--accent)'}}>{user?.username}</span>
            &nbsp;|&nbsp; role: <span style={{color:'var(--accent2)'}}>{user?.role}</span>
          </div>
          <div className="mono top-bar-time" id="clock" />
        </div>
        <Outlet />
      </main>
    </div>
  );
}
