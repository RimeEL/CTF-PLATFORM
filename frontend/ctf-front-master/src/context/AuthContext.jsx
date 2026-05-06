import { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      api.getMe().then(data => {
        if (!data.error) setUser(data);
        else localStorage.removeItem('token');
      }).finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = async (username, password) => {
    const data = await api.login(username, password);
    if (data.token) {
      localStorage.setItem('token', data.token);
      // Small delay to ensure token is saved
      await new Promise(resolve => setTimeout(resolve, 100));
      const me = await api.getMe();
      setUser(me);
      return { success: true };
    }
    return { success: false, error: data.error };
  };
  const register = async (username, email, password) => {
    const data = await api.register(username, email, password);
    if (data.message) return { success: true };
    return { success: false, error: data.error };
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
