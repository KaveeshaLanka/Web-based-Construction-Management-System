package com.lankabuild.cms.service;

import com.lankabuild.cms.dao.ProjectDao;
import com.lankabuild.cms.dao.TaskDao;
import com.lankabuild.cms.dao.UserDao;
import com.lankabuild.cms.exception.ApiException;
import com.lankabuild.cms.model.Role;
import com.lankabuild.cms.model.Task;
import com.lankabuild.cms.model.TaskStatus;
import com.lankabuild.cms.model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for Task Assignment and Tracking (section 3.2 / 6.2 of the requirements).
 * Project Managers and Site Supervisors can create/assign/update tasks.
 * Construction Workers can view their own tasks and update status only.
 */
public class TaskService {

    private final TaskDao taskDao = new TaskDao();
    private final ProjectDao projectDao = new ProjectDao();
    private final UserDao userDao = new UserDao();

    private void requireAssigner(Role role) {
        if (role != Role.PROJECT_MANAGER && role != Role.SITE_SUPERVISOR) {
            throw new ApiException(403, "Only Project Managers or Site Supervisors can perform this action");
        }
    }

    /** Makes sure a task is only ever assigned to a real user who is a Construction Worker. */
    private void validateAssignee(Integer assignedTo) {
        if (assignedTo == null) return; // unassigned is allowed
        try {
            User assignee = userDao.findById(assignedTo)
                    .orElseThrow(() -> new ApiException(400, "Assigned user does not exist"));
            if (assignee.getRole() != Role.CONSTRUCTION_WORKER) {
                throw new ApiException(400, "Tasks can only be assigned to a Construction Worker");
            }
        } catch (SQLException e) {
            throw new ApiException(500, "Database error validating assignee: " + e.getMessage());
        }
    }

    public Task createTask(Task task, Role requesterRole) {
        requireAssigner(requesterRole);
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new ApiException(400, "Task title is required");
        }
        validateAssignee(task.getAssignedTo());
        try {
            projectDao.findById(task.getProjectId())
                    .orElseThrow(() -> new ApiException(404, "Project not found"));
            if (task.getStatus() == null) task.setStatus(TaskStatus.PENDING);
            return taskDao.create(task);
        } catch (SQLException e) {
            throw new ApiException(500, "Database error creating task: " + e.getMessage());
        }
    }

    public List<Task> getAllTasks(Role requesterRole) {
        if (requesterRole == Role.CONSTRUCTION_WORKER) {
            throw new ApiException(403, "Use /api/tasks/my-tasks to view your assigned tasks");
        }
        try {
            return taskDao.findAll();
        } catch (SQLException e) {
            throw new ApiException(500, "Database error fetching tasks: " + e.getMessage());
        }
    }

    public List<Task> getTasksByProject(int projectId, Role requesterRole) {
        try {
            return taskDao.findByProjectId(projectId);
        } catch (SQLException e) {
            throw new ApiException(500, "Database error fetching tasks: " + e.getMessage());
        }
    }

    public List<Task> getMyTasks(int userId) {
        try {
            return taskDao.findByAssignedTo(userId);
        } catch (SQLException e) {
            throw new ApiException(500, "Database error fetching your tasks: " + e.getMessage());
        }
    }

    public Task getTaskById(int id, Role requesterRole, int requesterId) {
        try {
            Task task = taskDao.findById(id).orElseThrow(() -> new ApiException(404, "Task not found"));
            if (requesterRole == Role.CONSTRUCTION_WORKER &&
                    (task.getAssignedTo() == null || task.getAssignedTo() != requesterId)) {
                throw new ApiException(403, "You can only view tasks assigned to you");
            }
            return task;
        } catch (SQLException e) {
            throw new ApiException(500, "Database error fetching task: " + e.getMessage());
        }
    }

    public Task updateTask(int id, Task updated, Role requesterRole) {
        requireAssigner(requesterRole);
        if (updated.getAssignedTo() != null) {
            validateAssignee(updated.getAssignedTo());
        }
        try {
            Task existing = taskDao.findById(id).orElseThrow(() -> new ApiException(404, "Task not found"));
            existing.setTitle(updated.getTitle() != null ? updated.getTitle() : existing.getTitle());
            existing.setDescription(updated.getDescription() != null ? updated.getDescription() : existing.getDescription());
            existing.setAssignedTo(updated.getAssignedTo() != null ? updated.getAssignedTo() : existing.getAssignedTo());
            existing.setDeadline(updated.getDeadline() != null ? updated.getDeadline() : existing.getDeadline());
            existing.setStatus(updated.getStatus() != null ? updated.getStatus() : existing.getStatus());
            taskDao.update(existing);
            return existing;
        } catch (SQLException e) {
            throw new ApiException(500, "Database error updating task: " + e.getMessage());
        }
    }

    /** Construction Workers (and managers/supervisors) can update just the status of a task. */
    public Task updateTaskStatus(int id, TaskStatus newStatus, Role requesterRole, int requesterId) {
        try {
            Task existing = taskDao.findById(id).orElseThrow(() -> new ApiException(404, "Task not found"));

            if (requesterRole == Role.CONSTRUCTION_WORKER) {
                if (existing.getAssignedTo() == null || existing.getAssignedTo() != requesterId) {
                    throw new ApiException(403, "You can only update the status of tasks assigned to you");
                }
            } else if (requesterRole != Role.PROJECT_MANAGER && requesterRole != Role.SITE_SUPERVISOR) {
                throw new ApiException(403, "You do not have permission to update task status");
            }

            taskDao.updateStatus(id, newStatus);
            existing.setStatus(newStatus);
            return existing;
        } catch (SQLException e) {
            throw new ApiException(500, "Database error updating task status: " + e.getMessage());
        }
    }

    public void deleteTask(int id, Role requesterRole) {
        requireAssigner(requesterRole);
        try {
            boolean deleted = taskDao.delete(id);
            if (!deleted) throw new ApiException(404, "Task not found");
        } catch (SQLException e) {
            throw new ApiException(500, "Database error deleting task: " + e.getMessage());
        }
    }
}