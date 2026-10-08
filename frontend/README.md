# LankaBuild Construction Management System — Frontend

Plain HTML, CSS, and JavaScript (no frameworks, no build step) that talks to
your Java backend at `http://localhost:8080`.

## Files

```
construction-management-frontend/
├── index.html          # entry point, redirects to login or dashboard
├── login.html
├── signup.html
├── dashboard.html       # main app: Projects, Tasks, My Tasks tabs
├── css/style.css
└── js/
    ├── api.js           # fetch wrapper + session storage (talks to the backend)
    ├── login.js
    ├── signup.js
    └── dashboard.js     # all CRUD logic for projects, milestones, tasks
```

## 1. Make sure the backend is running first

The frontend expects the backend at `http://localhost:8080`. Start it exactly as before:
```bash
java -jar target/cms-backend.jar
```
(from the `construction-management-backend` project.)

If your backend ever runs on a different port, update this one line in `js/api.js`:
```js
const API_BASE_URL = "http://localhost:8080";
```

## 2. Serve the frontend (don't just double-click the HTML files)

Browsers sometimes block `fetch()` calls when a page is opened directly as a `file://` path.
Serve it over a local server instead — either works:

**Option A — VS Code Live Server extension (easiest)**
1. In VS Code, install the **"Live Server"** extension (by Ritwick Dey).
2. Open the `construction-management-frontend` folder in VS Code.
3. Right-click `login.html` → **"Open with Live Server"**.
4. It'll open in your browser at something like `http://127.0.0.1:5500/login.html`.

**Option B — Python's built-in server**
```bash
cd construction-management-frontend
python -m http.server 5500
```
Then open `http://localhost:5500/login.html` in your browser.

## 3. Using the app

1. Go to **Sign Up**, create a few accounts with different roles (e.g. one `PROJECT_MANAGER`, one `SITE_SUPERVISOR`, one `CONSTRUCTION_WORKER`). After signup, note the **User ID** shown on screen — Construction Workers need to share this ID with their Project Manager/Site Supervisor so they can be assigned tasks.
2. Log in as the **Project Manager** to create projects and set milestones.
3. Log in as the **Project Manager or Site Supervisor** to create tasks and assign them to a worker (by their User ID).
4. Log in as the **Construction Worker** to see "My Tasks" and update task status.
5. **Operations Manager** logs in to view all projects and tasks (read-only, per the requirements).

## Known limitation (by design, to keep things simple)

Assigning a task currently requires typing in the worker's **numeric User ID**, since
the backend doesn't yet expose a "list all users" endpoint. If you'd like, a future
addition could add a `GET /api/users?role=CONSTRUCTION_WORKER` endpoint and a proper
dropdown here instead of a raw ID field.
