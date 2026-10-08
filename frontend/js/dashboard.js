requireLogin();

const role = getUserRole();
document.getElementById("userEmailLabel").textContent = getUserEmail();
document.getElementById("userRoleLabel").textContent = role.replaceAll("_", " ");

document.getElementById("logoutBtn").addEventListener("click", () => {
  clearSession();
  window.location.href = "login.html";
});

// ---------- Role-based tab visibility ----------
const canManageProjects = role === "PROJECT_MANAGER";
const canViewProjects = ["PROJECT_MANAGER", "OPERATIONS_MANAGER", "SITE_SUPERVISOR"].includes(role);
const canAssignTasks = ["PROJECT_MANAGER", "SITE_SUPERVISOR"].includes(role);
const canViewAllTasks = ["PROJECT_MANAGER", "SITE_SUPERVISOR", "OPERATIONS_MANAGER"].includes(role);
const isWorker = role === "CONSTRUCTION_WORKER";

if (!canViewProjects) document.getElementById("tabBtnProjects").classList.add("hidden");
if (!canViewAllTasks) document.getElementById("tabBtnTasks").classList.add("hidden");
if (!isWorker) document.getElementById("tabBtnMyTasks").classList.add("hidden");
if (!canManageProjects) document.getElementById("newProjectBtn").classList.add("hidden");
if (!canAssignTasks) document.getElementById("newTaskBtn").classList.add("hidden");

// ---------- Tab switching ----------
const tabButtons = document.querySelectorAll(".tab-btn");
tabButtons.forEach(btn => {
  btn.addEventListener("click", () => {
    if (btn.classList.contains("hidden")) return;
    tabButtons.forEach(b => b.classList.remove("active"));
    document.querySelectorAll(".tab-content").forEach(c => c.classList.add("hidden"));
    btn.classList.add("active");
    document.getElementById(btn.dataset.tab).classList.remove("hidden");
    if (btn.dataset.tab === "projectsTab") loadProjects();
    if (btn.dataset.tab === "tasksTab") loadTasks();
    if (btn.dataset.tab === "myTasksTab") loadMyTasks();
  });
});

// Show the first available tab on load
if (canViewProjects) { loadProjects(); }
else if (canViewAllTasks) { document.getElementById("tabBtnTasks").click(); }
else if (isWorker) { document.getElementById("tabBtnMyTasks").click(); }

function statusBadge(status) {
  return '<span class="badge badge-' + status.toLowerCase() + '">' + status.replaceAll("_", " ") + '</span>';
}

function formatMoney(value) {
  if (value === null || value === undefined) return "-";
  return "LKR " + Number(value).toLocaleString();
}

// =====================================================================
// PROJECTS
// =====================================================================
let projectsCache = [];

async function loadProjects() {
  const tbody = document.getElementById("projectsTableBody");
  const emptyState = document.getElementById("projectsEmptyState");
  try {
    projectsCache = await apiRequest("/api/projects");
    tbody.innerHTML = "";
    emptyState.classList.toggle("hidden", projectsCache.length > 0);

    projectsCache.forEach(p => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${p.name}</td>
        <td>${p.clientName || "-"}</td>
        <td>${p.location || "-"}</td>
        <td class="col-status">${statusBadge(p.status)}</td>
        <td class="col-date">${p.startDate || "-"}</td>
        <td class="col-date">${p.endDate || "-"}</td>
        <td class="col-money">${formatMoney(p.budget)}</td>
        <td class="col-actions row-actions">
          <button class="btn btn-secondary btn-small" onclick="openMilestones(${p.id}, '${p.name.replace(/'/g, "\\'")}')">Milestones</button>
          ${canManageProjects ? `
            <button class="btn btn-secondary btn-small" onclick="openEditProject(${p.id})">Edit</button>
            <button class="btn btn-danger btn-small" onclick="deleteProject(${p.id})">Delete</button>
          ` : ""}
        </td>`;
      tbody.appendChild(tr);
    });
  } catch (err) {
    alert("Failed to load projects: " + err.message);
  }
}

document.getElementById("newProjectBtn").addEventListener("click", () => {
  document.getElementById("projectForm").reset();
  document.getElementById("projectId").value = "";
  document.getElementById("projectModalTitle").textContent = "New Project";
  document.getElementById("projectFormError").style.display = "none";
  document.getElementById("projectModal").classList.remove("hidden");
});

document.getElementById("cancelProjectBtn").addEventListener("click", () => {
  document.getElementById("projectModal").classList.add("hidden");
});

window.openEditProject = function (id) {
  const p = projectsCache.find(x => x.id === id);
  if (!p) return;
  document.getElementById("projectModalTitle").textContent = "Edit Project";
  document.getElementById("projectId").value = p.id;
  document.getElementById("projectName").value = p.name || "";
  document.getElementById("projectDescription").value = p.description || "";
  document.getElementById("projectClient").value = p.clientName || "";
  document.getElementById("projectLocation").value = p.location || "";
  document.getElementById("projectStartDate").value = p.startDate || "";
  document.getElementById("projectEndDate").value = p.endDate || "";
  document.getElementById("projectBudget").value = p.budget || "";
  document.getElementById("projectStatus").value = p.status || "PLANNING";
  document.getElementById("projectFormError").style.display = "none";
  document.getElementById("projectModal").classList.remove("hidden");
};

document.getElementById("projectForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const errorBox = document.getElementById("projectFormError");
  errorBox.style.display = "none";

  const id = document.getElementById("projectId").value;
  const payload = {
    name: document.getElementById("projectName").value.trim(),
    description: document.getElementById("projectDescription").value.trim(),
    clientName: document.getElementById("projectClient").value.trim(),
    location: document.getElementById("projectLocation").value.trim(),
    startDate: document.getElementById("projectStartDate").value || null,
    endDate: document.getElementById("projectEndDate").value || null,
    budget: document.getElementById("projectBudget").value ? Number(document.getElementById("projectBudget").value) : null,
    status: document.getElementById("projectStatus").value
  };

  try {
    if (id) {
      await apiRequest("/api/projects/" + id, "PUT", payload);
    } else {
      await apiRequest("/api/projects", "POST", payload);
    }
    document.getElementById("projectModal").classList.add("hidden");
    loadProjects();
  } catch (err) {
    errorBox.textContent = err.message;
    errorBox.style.display = "block";
  }
});

window.deleteProject = async function (id) {
  if (!confirm("Delete this project? This cannot be undone.")) return;
  try {
    await apiRequest("/api/projects/" + id, "DELETE");
    loadProjects();
  } catch (err) {
    alert("Failed to delete project: " + err.message);
  }
};

// =====================================================================
// MILESTONES
// =====================================================================
window.openMilestones = async function (projectId, projectName) {
  document.getElementById("milestoneProjectId").value = projectId;
  document.getElementById("milestoneProjectName").textContent = projectName;
  document.getElementById("milestoneForm").classList.toggle("hidden", !canManageProjects);
  await loadMilestones(projectId);
  document.getElementById("milestonesModal").classList.remove("hidden");
};

async function loadMilestones(projectId) {
  const tbody = document.getElementById("milestonesTableBody");
  const emptyState = document.getElementById("milestonesEmptyState");
  try {
    const milestones = await apiRequest("/api/projects/" + projectId + "/milestones");
    tbody.innerHTML = "";
    emptyState.classList.toggle("hidden", milestones.length > 0);
    milestones.forEach(m => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${m.title}</td>
        <td class="col-date">${m.dueDate || "-"}</td>
        <td class="col-status">${statusBadge(m.status)}</td>
        <td class="col-actions row-actions">
          ${canManageProjects ? `
            <button class="btn btn-secondary btn-small" onclick="markMilestone(${m.id}, ${projectId}, 'ACHIEVED')">Mark Achieved</button>
            <button class="btn btn-danger btn-small" onclick="deleteMilestone(${m.id}, ${projectId})">Delete</button>
          ` : ""}
        </td>`;
      tbody.appendChild(tr);
    });
  } catch (err) {
    alert("Failed to load milestones: " + err.message);
  }
}

document.getElementById("closeMilestonesBtn").addEventListener("click", () => {
  document.getElementById("milestonesModal").classList.add("hidden");
});

document.getElementById("milestoneForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const errorBox = document.getElementById("milestoneFormError");
  errorBox.style.display = "none";
  const projectId = document.getElementById("milestoneProjectId").value;

  const payload = {
    title: document.getElementById("milestoneTitle").value.trim(),
    dueDate: document.getElementById("milestoneDueDate").value || null,
    status: document.getElementById("milestoneStatus").value
  };

  try {
    await apiRequest("/api/projects/" + projectId + "/milestones", "POST", payload);
    document.getElementById("milestoneForm").reset();
    loadMilestones(projectId);
  } catch (err) {
    errorBox.textContent = err.message;
    errorBox.style.display = "block";
  }
});

window.markMilestone = async function (milestoneId, projectId, newStatus) {
  try {
    await apiRequest("/api/milestones/" + milestoneId, "PUT", { status: newStatus });
    loadMilestones(projectId);
  } catch (err) {
    alert("Failed to update milestone: " + err.message);
  }
};

window.deleteMilestone = async function (milestoneId, projectId) {
  if (!confirm("Delete this milestone?")) return;
  try {
    await apiRequest("/api/milestones/" + milestoneId, "DELETE");
    loadMilestones(projectId);
  } catch (err) {
    alert("Failed to delete milestone: " + err.message);
  }
};

// =====================================================================
// TASKS (Project Manager / Site Supervisor / Operations Manager view)
// =====================================================================
let tasksCache = [];
let projectNameById = {};
let workersCache = [];
let workerNameById = {};

async function loadWorkers() {
  workersCache = await getWorkers();
  workerNameById = {};
  workersCache.forEach(w => workerNameById[w.id] = w.fullName);
  populateWorkerDropdown();
}

function populateWorkerDropdown() {
  const select = document.getElementById("taskAssignedTo");
  const currentValue = select.value;
  select.innerHTML = '<option value="">Unassigned</option>';
  workersCache.forEach(w => {
    const opt = document.createElement("option");
    opt.value = w.id;
    opt.textContent = w.fullName + " (" + w.email + ")";
    select.appendChild(opt);
  });
  select.value = currentValue;
}

async function loadTasks() {
  const tbody = document.getElementById("tasksTableBody");
  const emptyState = document.getElementById("tasksEmptyState");
  try {
    // Load projects too, so we can show project names instead of raw IDs
    const projects = await apiRequest("/api/projects");
    projectNameById = {};
    projects.forEach(p => projectNameById[p.id] = p.name);
    populateProjectDropdown(projects);

    // Load workers so we can show names instead of raw IDs, and populate the assign dropdown
    await loadWorkers();

    tasksCache = await apiRequest("/api/tasks");
    tbody.innerHTML = "";
    emptyState.classList.toggle("hidden", tasksCache.length > 0);

    tasksCache.forEach(t => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${projectNameById[t.projectId] || ("Project #" + t.projectId)}</td>
        <td>${t.title}</td>
        <td>${t.assignedTo ? (workerNameById[t.assignedTo] || ("User #" + t.assignedTo)) : "Unassigned"}</td>
        <td class="col-date">${t.deadline || "-"}</td>
        <td class="col-status">${statusBadge(t.status)}</td>
        <td class="col-actions row-actions">
          <button class="btn btn-secondary btn-small" onclick="openEditTask(${t.id})">Edit</button>
          <button class="btn btn-danger btn-small" onclick="deleteTask(${t.id})">Delete</button>
        </td>`;
      tbody.appendChild(tr);
    });
  } catch (err) {
    alert("Failed to load tasks: " + err.message);
  }
}

function populateProjectDropdown(projects) {
  const select = document.getElementById("taskProjectId");
  select.innerHTML = "";
  projects.forEach(p => {
    const opt = document.createElement("option");
    opt.value = p.id;
    opt.textContent = p.name;
    select.appendChild(opt);
  });
}

document.getElementById("newTaskBtn").addEventListener("click", () => {
  document.getElementById("taskForm").reset();
  document.getElementById("taskId").value = "";
  document.getElementById("taskModalTitle").textContent = "New Task";
  document.getElementById("taskFormError").style.display = "none";
  document.getElementById("taskModal").classList.remove("hidden");
});

document.getElementById("cancelTaskBtn").addEventListener("click", () => {
  document.getElementById("taskModal").classList.add("hidden");
});

window.openEditTask = function (id) {
  const t = tasksCache.find(x => x.id === id);
  if (!t) return;
  document.getElementById("taskModalTitle").textContent = "Edit Task";
  document.getElementById("taskId").value = t.id;
  document.getElementById("taskProjectId").value = t.projectId;
  document.getElementById("taskTitle").value = t.title || "";
  document.getElementById("taskDescription").value = t.description || "";
  document.getElementById("taskAssignedTo").value = t.assignedTo || "";
  document.getElementById("taskDeadline").value = t.deadline || "";
  document.getElementById("taskStatus").value = t.status || "PENDING";
  document.getElementById("taskFormError").style.display = "none";
  document.getElementById("taskModal").classList.remove("hidden");
};

document.getElementById("taskForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const errorBox = document.getElementById("taskFormError");
  errorBox.style.display = "none";

  const id = document.getElementById("taskId").value;
  const assignedRaw = document.getElementById("taskAssignedTo").value;

  const payload = {
    projectId: Number(document.getElementById("taskProjectId").value),
    title: document.getElementById("taskTitle").value.trim(),
    description: document.getElementById("taskDescription").value.trim(),
    assignedTo: assignedRaw ? Number(assignedRaw) : null,
    deadline: document.getElementById("taskDeadline").value || null,
    status: document.getElementById("taskStatus").value
  };

  try {
    if (id) {
      await apiRequest("/api/tasks/" + id, "PUT", payload);
    } else {
      await apiRequest("/api/tasks", "POST", payload);
    }
    document.getElementById("taskModal").classList.add("hidden");
    loadTasks();
  } catch (err) {
    errorBox.textContent = err.message;
    errorBox.style.display = "block";
  }
});

window.deleteTask = async function (id) {
  if (!confirm("Delete this task?")) return;
  try {
    await apiRequest("/api/tasks/" + id, "DELETE");
    loadTasks();
  } catch (err) {
    alert("Failed to delete task: " + err.message);
  }
};

// =====================================================================
// MY TASKS (Construction Worker)
// =====================================================================
async function loadMyTasks() {
  const tbody = document.getElementById("myTasksTableBody");
  const emptyState = document.getElementById("myTasksEmptyState");
  try {
    const tasks = await apiRequest("/api/tasks/my-tasks");
    tbody.innerHTML = "";
    emptyState.classList.toggle("hidden", tasks.length > 0);

    tasks.forEach(t => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${t.title}</td>
        <td>${t.description || "-"}</td>
        <td class="col-date">${t.deadline || "-"}</td>
        <td class="col-status" style="width:160px; overflow:visible;">
          <select style="width:100%;" onchange="updateMyTaskStatus(${t.id}, this.value)">
            <option value="PENDING" ${t.status === "PENDING" ? "selected" : ""}>Pending</option>
            <option value="IN_PROGRESS" ${t.status === "IN_PROGRESS" ? "selected" : ""}>In Progress</option>
            <option value="COMPLETED" ${t.status === "COMPLETED" ? "selected" : ""}>Completed</option>
            <option value="DELAYED" ${t.status === "DELAYED" ? "selected" : ""}>Delayed</option>
          </select>
        </td>`;
      tbody.appendChild(tr);
    });
  } catch (err) {
    alert("Failed to load your tasks: " + err.message);
  }
}

window.updateMyTaskStatus = async function (taskId, newStatus) {
  try {
    await apiRequest("/api/tasks/" + taskId + "/status", "PATCH", { status: newStatus });
  } catch (err) {
    alert("Failed to update status: " + err.message);
    loadMyTasks();
  }
};