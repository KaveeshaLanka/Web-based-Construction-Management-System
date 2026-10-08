document.getElementById("signupForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const errorBox = document.getElementById("errorBox");
  const successBox = document.getElementById("successBox");
  errorBox.style.display = "none";
  successBox.style.display = "none";

  const fullName = document.getElementById("fullName").value.trim();
  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;
  const role = document.getElementById("role").value;

  try {
    const created = await apiRequest("/api/auth/signup", "POST", { fullName, email, password, role });
    successBox.innerHTML = "Account created! Your <strong>User ID is " + created.id +
      "</strong> &mdash; if you're a Construction Worker, share this ID with your Project Manager " +
      "so tasks can be assigned to you. Redirecting to login...";
    successBox.style.display = "block";
    setTimeout(() => { window.location.href = "login.html"; }, 3500);
  } catch (err) {
    errorBox.textContent = err.message;
    errorBox.style.display = "block";
  }
});
