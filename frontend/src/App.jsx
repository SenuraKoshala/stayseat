import React, { useEffect, useState } from 'react';
import { api, isAuthed, setToken } from './api.js';
import AuthPage from './pages/AuthPage.jsx';
import HotelsPage from './pages/HotelsPage.jsx';
import RestaurantsPage from './pages/RestaurantsPage.jsx';
import BookingsPage from './pages/BookingsPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';

const TABS = [
  { key: 'hotels', label: 'Hotels' },
  { key: 'restaurants', label: 'Restaurants' },
  { key: 'bookings', label: 'My Bookings' },
  { key: 'profile', label: 'Profile' },
];

export default function App() {
  const [authed, setAuthed] = useState(isAuthed());
  const [view, setView] = useState('hotels');
  const [greeting, setGreeting] = useState('');

  useEffect(() => {
    if (!authed) return;
    api
      .me()
      .then((r) => {
        const p = r.data;
        const name = [p.firstName, p.lastName].filter(Boolean).join(' ');
        setGreeting(name || p.role || '');
      })
      .catch(() => setGreeting(''));
  }, [authed]);

  function logout() {
    setToken(null);
    setAuthed(false);
    setView('hotels');
  }

  if (!authed) {
    return <AuthPage onAuthed={() => setAuthed(true)} />;
  }

  return (
    <div>
      <header className="topbar">
        <div className="brand">Stay<span>Seat</span></div>
        <nav className="nav">
          {TABS.map((t) => (
            <button
              key={t.key}
              className={view === t.key ? 'active' : ''}
              onClick={() => setView(t.key)}
            >
              {t.label}
            </button>
          ))}
        </nav>
        {greeting && <span className="subtle" style={{ marginRight: 12 }}>Hi, {greeting}</span>}
        <button className="link" onClick={logout}>Log out</button>
      </header>

      <main className="container">
        {view === 'hotels' && <HotelsPage />}
        {view === 'restaurants' && <RestaurantsPage />}
        {view === 'bookings' && <BookingsPage />}
        {view === 'profile' && <ProfilePage onProfileChange={(name) => setGreeting(name)} />}
      </main>
    </div>
  );
}
