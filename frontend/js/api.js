// ---------------------------------------------------------
// Central place for talking to the Java backend.
// Change API_BASE_URL if your backend runs on a different port.
// ---------------------------------------------------------
const API_BASE_URL = "http://localhost:8080";

function getToken() {
  return localStorage.getItem("cms_token");
}

function getUserRole() {
  return localStorage.getItem("cms_role");
}

function getUserEmail() {
  return localStorage.getItem("cms_email");
}

/**
 * Decodes the JWT payload (no verification — the backend already
 * verified it) to pull out the logged-in user's numeric ID.
 * Returns null if there's no token or it can't be parsed.
 */
function getUserId() {
  const token = getToken();
  if (!token) return null;
  try {
    const payload = token.split(".")[1];
    const decoded = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
    return decoded.sub ? parseInt(decoded.sub, 10) : null;
  } catch (e) {
    return null;
  }
}

function saveSession(token, role, email) {
  localStorage.setItem("cms_token", token);
  localStorage.setItem("cms_role", role);
  localStorage.setItem("cms_email", email);
}

function clearSession() {
  localStorage.removeItem("cms_token");
  localStorage.removeItem("cms_role");
  localStorage.removeItem("cms_email");
}

function requireLogin() {
  if (!getToken()) {
    window.location.href = "login.html";
  }
}

/**
 * Wrapper around fetch() that adds the base URL, JSON headers,
 * the Authorization header (if logged in), and handles error responses.
 */
async function apiRequest(path, method = "GET", body = null) {
  const headers = { "Content-Type": "application/json" };
  const token = getToken();
  if (token) headers["Authorization"] = "Bearer " + token;

  const options = { method, headers };
  if (body !== null) options.body = JSON.stringify(body);

  let response;
  try {
    response = await fetch(API_BASE_URL + path, options);
  } catch (networkErr) {
    throw new Error("Could not reach the backend. Is it running at " + API_BASE_URL + "?");
  }

  let data = null;
  const text = await response.text();
  if (text) {
    try { data = JSON.parse(text); } catch (e) { data = null; }
  }

  if (!response.ok) {
    const message = (data && data.error) ? data.error : ("Request failed with status " + response.status);
    if (response.status === 401) {
      clearSession();
      window.location.href = "login.html";
    }
    throw new Error(message);
  }

  return data;
}

// ---------------------------------------------------------
// Users
// ---------------------------------------------------------

/**
 * Fetches users, optionally filtered by role.
 * e.g. getUsers("CONSTRUCTION_WORKER") for the assign-task dropdown.
 * Returns an array of { id, fullName, email, role }.
 */
async function getUsers(role = null) {
  const query = role ? ("?role=" + encodeURIComponent(role)) : "";
  const data = await apiRequest("/api/users" + query, "GET");
  return (data && data.users) ? data.users : [];
}

/** Convenience wrapper for populating the assign-task worker dropdown. */
async function getWorkers() {
  return getUsers("CONSTRUCTION_WORKER");
}