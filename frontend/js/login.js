// Decodes the payload of a JWT (no signature verification needed here -
// the backend already verified it; we just want to read email/role for the UI).
function decodeJwtPayload(token) {
  const payload = token.split(".")[1];
  const decoded = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
  return JSON.parse(decoded);
}

document.getElementById("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const errorBox = document.getElementById("errorBox");
  errorBox.style.display = "none";

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;

  try {
    const result = await apiRequest("/api/auth/login", "POST", { email, password });
    const claims = decodeJwtPayload(result.token);
    saveSession(result.token, claims.role, claims.email);
    window.location.href = "dashboard.html";
  } catch (err) {
    errorBox.textContent = err.message;
    errorBox.style.display = "block";
  }
});
