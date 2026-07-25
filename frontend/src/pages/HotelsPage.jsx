import React, { useEffect, useState } from 'react';
import { api } from '../api.js';

const iso = (d) => d.toISOString().slice(0, 10);
const plusDays = (n) => { const d = new Date(); d.setDate(d.getDate() + n); return iso(d); };

export default function HotelsPage() {
  const [properties, setProperties] = useState([]);
  const [selected, setSelected] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [checkIn, setCheckIn] = useState(plusDays(1));
  const [checkOut, setCheckOut] = useState(plusDays(2));
  const [msg, setMsg] = useState(null); // {type, text}
  const [busyRoom, setBusyRoom] = useState(null);

  useEffect(() => {
    api.hotelProperties().then((r) => setProperties(r.data || []))
      .catch((e) => setMsg({ type: 'error', text: e.message }));
  }, []);

  async function openProperty(p) {
    setSelected(p);
    setRooms([]);
    setMsg(null);
    try {
      const r = await api.hotelRooms(p.id);
      setRooms(r.data || []);
    } catch (e) {
      setMsg({ type: 'error', text: e.message });
    }
  }

  async function book(room) {
    setMsg(null);
    if (!checkIn || !checkOut || checkOut <= checkIn) {
      setMsg({ type: 'error', text: 'Check-out must be after check-in.' });
      return;
    }
    setBusyRoom(room.id);
    try {
      const r = await api.hotelBook({ roomId: room.id, checkInDate: checkIn, checkOutDate: checkOut });
      const b = r.data;
      setMsg({
        type: 'ok',
        text: `Booked room ${room.roomNumber} · ${b.status} · ${b.totalAmount.amount} ${b.totalAmount.currency}. See "My Bookings".`,
      });
    } catch (e) {
      setMsg({ type: 'error', text: e.message });
    } finally {
      setBusyRoom(null);
    }
  }

  return (
    <div>
      <div className="section-title">Hotels</div>
      {msg && <div className={`msg ${msg.type}`}>{msg.text}</div>}

      {!selected && (
        <>
          {properties.length === 0 && <p className="subtle">No properties yet. (Seed some, or add via an admin account.)</p>}
          <div className="grid">
            {properties.map((p) => (
              <div className="card" key={p.id}>
                <h3>{p.name}</h3>
                <div className="muted">{p.city}{p.address ? ` · ${p.address}` : ''}</div>
                {p.description && <p className="subtle">{p.description}</p>}
                <button className="btn ghost" onClick={() => openProperty(p)}>View rooms</button>
              </div>
            ))}
          </div>
        </>
      )}

      {selected && (
        <>
          <button className="link" onClick={() => { setSelected(null); setMsg(null); }}>&larr; Back to hotels</button>
          <h3 style={{ marginTop: 8 }}>{selected.name} — {selected.city}</h3>

          <div className="card" style={{ marginBottom: 16 }}>
            <div className="row">
              <div>
                <label>Check-in</label>
                <input type="date" value={checkIn} onChange={(e) => setCheckIn(e.target.value)} />
              </div>
              <div>
                <label>Check-out</label>
                <input type="date" value={checkOut} onChange={(e) => setCheckOut(e.target.value)} />
              </div>
            </div>
          </div>

          {rooms.length === 0 && <p className="subtle">No rooms for this property.</p>}
          <div className="grid">
            {rooms.map((room) => (
              <div className="card" key={room.id}>
                <h3>Room {room.roomNumber} <span className="pill">{room.type}</span></h3>
                <div className="muted">Sleeps {room.capacity}</div>
                <div style={{ fontWeight: 700, margin: '6px 0' }}>
                  {room.pricePerNight.amount} {room.pricePerNight.currency} <span className="subtle">/ night</span>
                </div>
                <button className="btn" disabled={busyRoom === room.id} onClick={() => book(room)}>
                  {busyRoom === room.id ? 'Booking…' : 'Book'}
                </button>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
