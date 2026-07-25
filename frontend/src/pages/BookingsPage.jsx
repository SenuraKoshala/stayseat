import React, { useEffect, useState } from 'react';
import { api } from '../api.js';

export default function BookingsPage() {
  const [hotel, setHotel] = useState([]);
  const [rest, setRest] = useState([]);
  const [msg, setMsg] = useState(null);
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    setMsg(null);
    try {
      const [h, r] = await Promise.all([api.hotelMyBookings(), api.restMyBookings()]);
      setHotel(h.data || []);
      setRest(r.data || []);
    } catch (e) {
      setMsg({ type: 'error', text: e.message });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function cancelHotel(id) {
    try { await api.hotelCancel(id); load(); }
    catch (e) { setMsg({ type: 'error', text: e.message }); }
  }
  async function cancelRest(id) {
    try { await api.restCancel(id); load(); }
    catch (e) { setMsg({ type: 'error', text: e.message }); }
  }

  const canCancel = (s) => s === 'PENDING' || s === 'CONFIRMED';

  return (
    <div>
      <div className="section-title">My Bookings</div>
      {msg && <div className={`msg ${msg.type}`}>{msg.text}</div>}
      {loading && <p className="subtle">Loading…</p>}

      <h3>Hotel rooms</h3>
      {!loading && hotel.length === 0 && <p className="subtle">No hotel bookings yet.</p>}
      <div className="grid">
        {hotel.map((b) => (
          <div className="card" key={b.id}>
            <div className="row" style={{ alignItems: 'center' }}>
              <h3 style={{ margin: 0 }}>Room booking</h3>
              <span className={`pill ${b.status}`}>{b.status}</span>
            </div>
            <div className="muted">{b.checkInDate} → {b.checkOutDate}</div>
            {b.totalAmount && <div style={{ fontWeight: 700, margin: '6px 0' }}>{b.totalAmount.amount} {b.totalAmount.currency}</div>}
            {canCancel(b.status) && <button className="btn danger" onClick={() => cancelHotel(b.id)}>Cancel</button>}
          </div>
        ))}
      </div>

      <h3 style={{ marginTop: 24 }}>Restaurant tables</h3>
      {!loading && rest.length === 0 && <p className="subtle">No restaurant bookings yet.</p>}
      <div className="grid">
        {rest.map((b) => (
          <div className="card" key={b.id}>
            <div className="row" style={{ alignItems: 'center' }}>
              <h3 style={{ margin: 0 }}>Table reservation</h3>
              <span className={`pill ${b.status}`}>{b.status}</span>
            </div>
            <div className="muted">{b.reservationDate} at {b.timeSlot} · party of {b.partySize}</div>
            {canCancel(b.status) && <button className="btn danger" onClick={() => cancelRest(b.id)}>Cancel</button>}
          </div>
        ))}
      </div>
    </div>
  );
}
