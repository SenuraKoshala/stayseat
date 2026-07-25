import React, { useEffect, useState } from 'react';
import { api } from '../api.js';

const iso = (d) => d.toISOString().slice(0, 10);
const plusDays = (n) => { const d = new Date(); d.setDate(d.getDate() + n); return iso(d); };

export default function RestaurantsPage() {
  const [properties, setProperties] = useState([]);
  const [selected, setSelected] = useState(null);
  const [tables, setTables] = useState([]);
  const [date, setDate] = useState(plusDays(1));
  const [timeSlot, setTimeSlot] = useState('19:30');
  const [partySize, setPartySize] = useState(2);
  const [msg, setMsg] = useState(null);
  const [busyTable, setBusyTable] = useState(null);

  useEffect(() => {
    api.restProperties().then((r) => setProperties(r.data || []))
      .catch((e) => setMsg({ type: 'error', text: e.message }));
  }, []);

  async function openProperty(p) {
    setSelected(p);
    setTables([]);
    setMsg(null);
    try {
      const r = await api.restTables(p.id);
      setTables(r.data || []);
    } catch (e) {
      setMsg({ type: 'error', text: e.message });
    }
  }

  async function book(table) {
    setMsg(null);
    setBusyTable(table.id);
    try {
      const r = await api.restBook({
        tableId: table.id,
        reservationDate: date,
        timeSlot,
        partySize: Number(partySize),
      });
      const b = r.data;
      setMsg({ type: 'ok', text: `Reserved table ${table.tableNumber} for ${b.partySize} at ${b.timeSlot} · ${b.status}. See "My Bookings".` });
    } catch (e) {
      setMsg({ type: 'error', text: e.message });
    } finally {
      setBusyTable(null);
    }
  }

  return (
    <div>
      <div className="section-title">Restaurants</div>
      {msg && <div className={`msg ${msg.type}`}>{msg.text}</div>}

      {!selected && (
        <>
          {properties.length === 0 && <p className="subtle">No restaurants yet.</p>}
          <div className="grid">
            {properties.map((p) => (
              <div className="card" key={p.id}>
                <h3>{p.name}</h3>
                <div className="muted">{p.city}{p.address ? ` · ${p.address}` : ''}</div>
                {p.description && <p className="subtle">{p.description}</p>}
                <button className="btn ghost" onClick={() => openProperty(p)}>View tables</button>
              </div>
            ))}
          </div>
        </>
      )}

      {selected && (
        <>
          <button className="link" onClick={() => { setSelected(null); setMsg(null); }}>&larr; Back to restaurants</button>
          <h3 style={{ marginTop: 8 }}>{selected.name} — {selected.city}</h3>

          <div className="card" style={{ marginBottom: 16 }}>
            <div className="row">
              <div>
                <label>Date</label>
                <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
              </div>
              <div>
                <label>Time</label>
                <input type="time" value={timeSlot} onChange={(e) => setTimeSlot(e.target.value)} />
              </div>
              <div>
                <label>Party size</label>
                <input type="number" min={1} value={partySize} onChange={(e) => setPartySize(e.target.value)} />
              </div>
            </div>
          </div>

          {tables.length === 0 && <p className="subtle">No tables for this restaurant.</p>}
          <div className="grid">
            {tables.map((table) => (
              <div className="card" key={table.id}>
                <h3>Table {table.tableNumber}</h3>
                <div className="muted">Seats {table.capacity}</div>
                <button className="btn" disabled={busyTable === table.id} onClick={() => book(table)}>
                  {busyTable === table.id ? 'Reserving…' : 'Reserve'}
                </button>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
