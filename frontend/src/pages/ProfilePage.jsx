import React, { useEffect, useRef, useState } from 'react';
import { api } from '../api.js';

export default function ProfilePage({ onProfileChange }) {
  const [profile, setProfile] = useState(null);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [msg, setMsg] = useState(null);
  const [busy, setBusy] = useState(false);
  const fileRef = useRef(null);

  function apply(p) {
    setProfile(p);
    setFirstName(p.firstName || '');
    setLastName(p.lastName || '');
    setPhone(p.phone || '');
  }

  useEffect(() => {
    api.me().then((r) => apply(r.data))
      .catch((e) => setMsg({ type: 'error', text: e.message === 'User profile not found.'
        ? 'Profile not created yet — it is created from the UserRegistered event. Make sure Auth + the broker were up when you registered.'
        : e.message }));
  }, []);

  async function save(e) {
    e.preventDefault();
    setBusy(true);
    setMsg(null);
    try {
      const r = await api.updateMe({ firstName, lastName, phone });
      apply(r.data);
      setMsg({ type: 'ok', text: 'Profile saved.' });
      if (onProfileChange) onProfileChange([r.data.firstName, r.data.lastName].filter(Boolean).join(' '));
    } catch (err) {
      setMsg({ type: 'error', text: err.message });
    } finally {
      setBusy(false);
    }
  }

  async function upload(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setMsg(null);
    try {
      const r = await api.uploadImage(file);
      setProfile((p) => ({ ...p, imageUrl: r.data.imageUrl }));
      setMsg({ type: 'ok', text: 'Image uploaded.' });
    } catch (err) {
      setMsg({ type: 'error', text: err.message });
    } finally {
      if (fileRef.current) fileRef.current.value = '';
    }
  }

  if (!profile && !msg) return <p className="subtle">Loading…</p>;

  return (
    <div>
      <div className="section-title">Profile</div>
      {msg && <div className={`msg ${msg.type}`}>{msg.text}</div>}

      {profile && (
        <div className="card" style={{ maxWidth: 480 }}>
          <div className="row" style={{ alignItems: 'center', marginBottom: 8 }}>
            <img
              className="avatar"
              style={{ flex: 'none' }}
              src={profile.imageUrl || 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="84" height="84"><rect width="84" height="84" fill="%23eef1f7"/></svg>'}
              alt="avatar"
            />
            <div>
              <div className="muted" style={{ fontSize: 12 }}>Role</div>
              <div><span className="pill">{profile.role}</span></div>
              <button className="link" style={{ marginTop: 8 }} onClick={() => fileRef.current?.click()}>Change photo</button>
              <input ref={fileRef} type="file" accept="image/*" hidden onChange={upload} />
            </div>
          </div>

          <form onSubmit={save}>
            <div className="row">
              <div>
                <label>First name</label>
                <input value={firstName} onChange={(e) => setFirstName(e.target.value)} />
              </div>
              <div>
                <label>Last name</label>
                <input value={lastName} onChange={(e) => setLastName(e.target.value)} />
              </div>
            </div>
            <label>Phone</label>
            <input value={phone} onChange={(e) => setPhone(e.target.value)} />
            <button className="btn" disabled={busy}>{busy ? 'Saving…' : 'Save profile'}</button>
          </form>
        </div>
      )}
    </div>
  );
}
