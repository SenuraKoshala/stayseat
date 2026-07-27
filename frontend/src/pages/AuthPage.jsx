import React, { useState } from 'react';
import { api, setToken } from '../api.js';

export default function AuthPage({ onAuthed }) {
  const [mode, setMode] = useState('login'); // 'login' | 'register'
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('CUSTOMER');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function submit(e) {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      if (mode === 'register') {
        await api.register({ email, password, role });
      }
      const res = await api.login({ email, password });
      setToken(res.data.accessToken);
      onAuthed();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-wrap">
      <div className="card auth-card">
        <div className="brand" style={{ fontSize: 26, marginBottom: 4 }}>Stay<span>Seat</span></div>
        <p className="subtle" style={{ marginTop: 0 }}>
          Unified hotel room &amp; restaurant table reservations
        </p>

        {error && <div className="msg error">{error}</div>}

        <form onSubmit={submit}>
          <label>Email</label>
          <input type="email" value={email} required onChange={(e) => setEmail(e.target.value)} />

          <label>Password</label>
          <input type="password" value={password} required minLength={8}
                 placeholder="At least 8 characters"
                 onChange={(e) => setPassword(e.target.value)} />
          {mode === 'register' && <div className="subtle" style={{ marginTop: 4 }}>Must be at least 8 characters.</div>}

          {mode === 'register' && (
            <>
              <label>Role</label>
              <select value={role} onChange={(e) => setRole(e.target.value)}>
                <option value="CUSTOMER">Customer</option>
                <option value="HOTEL_ADMIN">Hotel Admin</option>
                <option value="RESTAURANT_ADMIN">Restaurant Admin</option>
              </select>
            </>
          )}

          <button className="btn" style={{ width: '100%' }} disabled={busy}>
            {busy ? 'Please wait…' : mode === 'login' ? 'Log in' : 'Create account'}
          </button>
        </form>

        <div className="spacer" />
        <p className="subtle" style={{ textAlign: 'center' }}>
          {mode === 'login' ? "Don't have an account? " : 'Already registered? '}
          <button className="link" onClick={() => { setMode(mode === 'login' ? 'register' : 'login'); setError(''); }}>
            {mode === 'login' ? 'Register' : 'Log in'}
          </button>
        </p>
      </div>
    </div>
  );
}
