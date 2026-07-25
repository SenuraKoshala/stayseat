// Thin client for the StaySeat API Gateway.
// Every call goes through the gateway (default :8090), which validates the JWT
// and routes to the right microservice.

const BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8090';

export function getToken() {
  return localStorage.getItem('token');
}
export function setToken(t) {
  if (t) localStorage.setItem('token', t);
  else localStorage.removeItem('token');
}
export function isAuthed() {
  return !!getToken();
}

async function req(method, path, body, isForm) {
  const headers = {};
  const token = getToken();
  if (token) headers['Authorization'] = 'Bearer ' + token;

  let payload;
  if (isForm) {
    payload = body; // FormData — let the browser set the boundary
  } else if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    payload = JSON.stringify(body);
  }

  const res = await fetch(BASE + path, { method, headers, body: payload });

  let json = null;
  try {
    json = await res.json();
  } catch {
    /* empty / non-JSON response (e.g. 204) */
  }

  if (!res.ok || (json && json.success === false)) {
    const message = json?.error?.message || `Request failed (HTTP ${res.status})`;
    const err = new Error(message);
    err.status = res.status;
    err.code = json?.error?.code;
    throw err;
  }
  return json; // { success, data, meta }
}

export const api = {
  // Auth
  register: (b) => req('POST', '/api/v1/auth/register', b),
  login: (b) => req('POST', '/api/v1/auth/login', b),

  // User profile
  me: () => req('GET', '/api/v1/users/me'),
  updateMe: (b) => req('PUT', '/api/v1/users/me', b),
  uploadImage: (file) => {
    const fd = new FormData();
    fd.append('file', file);
    return req('POST', '/api/v1/users/me/image', fd, true);
  },

  // Hotel
  hotelProperties: (city) =>
    req('GET', '/api/v1/hotel/properties' + (city ? `?city=${encodeURIComponent(city)}` : '')),
  hotelRooms: (propertyId) => req('GET', `/api/v1/hotel/properties/${propertyId}/rooms`),
  hotelBook: (b) => req('POST', '/api/v1/hotel/bookings', b),
  hotelMyBookings: () => req('GET', '/api/v1/hotel/bookings/me'),
  hotelCancel: (id) => req('PATCH', `/api/v1/hotel/bookings/${id}/cancel`, { reason: 'Cancelled by user' }),

  // Restaurant
  restProperties: (city) =>
    req('GET', '/api/v1/restaurant/properties' + (city ? `?city=${encodeURIComponent(city)}` : '')),
  restTables: (propertyId) => req('GET', `/api/v1/restaurant/properties/${propertyId}/tables`),
  restBook: (b) => req('POST', '/api/v1/restaurant/bookings', b),
  restMyBookings: () => req('GET', '/api/v1/restaurant/bookings/me'),
  restCancel: (id) => req('PATCH', `/api/v1/restaurant/bookings/${id}/cancel`, { reason: 'Cancelled by user' }),
};

export { BASE };
