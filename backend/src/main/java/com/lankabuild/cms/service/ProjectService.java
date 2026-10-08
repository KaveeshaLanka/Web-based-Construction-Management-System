package com.lankabuild.cms.service;

import com.lankabuild.cms.dao.MilestoneDao;
import com.lankabuild.cms.dao.ProjectDao;
import com.lankabuild.cms.exception.ApiException;
import com.lankabuild.cms.model.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for Project Management (section 3.1 / 6.1 of the requirements).
 * Only Project Managers can create/update/delete projects.
 * Operations Managers and Project Managers can view.
 */
public class ProjectService {

    private final ProjectDao projectDao = new ProjectDao();
    private final MilestoneDao milestoneDao = new MilestoneDao();

    private void requireProjectManager(Role role) {
        if (role != Role.PROJECT_MANAGER) {
            throw new ApiException(403, "Only Project Managers can perform this action");
        }
    }

    private void requireViewAccess(Role role) {
        if (role != Role.PROJECT_MANAGER && role != Role.OPERATIONS_MANAGER && role != Role.SITE_SUPERVISOR) {
            throw new ApiException(403, "You do not have permission to view projects");
        }
    }

    public Project createProject(Project project, Role requesterRole, int requesterId) {
        requireProjectManager(requesterRole);
        if (project.getName() == null || project.getName().isBlank()) {
            throw new ApiException(400, "Project name is required");
        }
        if (project.getStatus() == null) project.setStatus(ProjectStatus.PLANNING);
        project.setCreatedBy(requesterId);
        try {
            return projectDao.create(project);
        } catch (SQLException e) {
            throw new ApiException(500, "Database error creating project: " + e.getMessage());
        }
    }

    public List<Project> getAllProjects(Role requesterRole) {
        requireViewAccess(requesterRole);
        try {
            return projectDao.findAll();
        } catch (SQLException e) {
            throw new ApiException(500, "Database error fetching projects: " + e.getMessage());
        }
    }

    public Project getProjectById(int id, Role requesterRole) {
        requireViewAccess(requesterRole);
        try {
            return projectDao.findById(id)
                    .orElseThrow(() -> new ApiException(404, "Project not found"));
        } catch (SQLException e) {
            throw new ApiException(500, "Database error fetching project: " + e.getMessage());
        }
    }

    public Project updateProject(int id, Project updated, Role requesterRole) {
        requireProjectManager(requesterRole);
        try {
            Project existing = projectDao.findById(id)
                    .orElseThrow(() -> new ApiException(404, "Project not found"));

            existing.setName(updated.getName() != null ? updated.getName() : existing.getName());
            existing.setDescription(updated.getDescription() != null ? updated.getDescription() : existing.getDescription());
            existing.setClientName(updated.getClientName() != null ? updated.getClientName() : existing.getClientName());
            existing.setLocation(updated.getLocation() != null ? updated.getLocation() : existing.getLocation());
            existing.setStartDate(updated.getStartDate() != null ? updated.getStartDate() : existing.getStartDate());
            existing.setEndDate(updated.getEndDate() != null ? updated.getEndDate() : existing.getEndDate());
            existing.setBudget(updated.getBudget() != null ? updated.getBudget() : existing.getBudget());
            existing.setStatus(updated.getStatus() != null ? updated.getStatus() : existing.getStatus());

            projectDao.update(existing);
            return existing;
        } catch (SQLException e) {
            throw new ApiException(500, "Database error updating project: " + e.getMessage());
        }
    }

    public void deleteProject(int id, Role requesterRole) {
        requireProjectManager(requesterRole);
        try {
            boolean deleted = projectDao.delete(id);
            if (!deleted) throw new ApiException(404, "Project not found");
        } catch (SQLException e) {
            throw new ApiException(500, "Database error deleting project: " + e.getMessage());
        }
    }

    // ---------- Milestones ----------

    public Milestone addMilestone(int projectId, Milestone milestone, Role requesterRole) {
        requireProjectManager(requesterRole);
        try {
            projectDao.findById(projectId).orElseThrow(() -> new ApiException(404, "Project not found"));
            milestone.setProjectId(projectId);
            if (milestone.getStatus() == null) milestone.setStatus(MilestoneStatus.PENDING);
            return milestoneDao.create(milestone);
        } catch (SQLException e) {
            throw new ApiException(500, "Database error creating milestone: " + e.getMessage());
        }
    }

    public List<Milestone> getMilestones(int projectId, Role requesterRole) {
        requireViewAccess(requesterRole);
        try {
            return milestoneDao.findByProjectId(projectId);
        } catch (SQLException e) {
            throw new ApiException(500, "Database error fetching milestones: " + e.getMessage());
        }
    }

    public Milestone updateMilestone(int milestoneId, Milestone updated, Role requesterRole) {
        requireProjectManager(requesterRole);
        try {
            Milestone existing = milestoneDao.findById(milestoneId)
                    .orElseThrow(() -> new ApiException(404, "Milestone not found"));
            existing.setTitle(updated.getTitle() != null ? updated.getTitle() : existing.getTitle());
            existing.setDescription(updated.getDescription() != null ? updated.getDescription() : existing.getDescription());
            existing.setDueDate(updated.getDueDate() != null ? updated.getDueDate() : existing.getDueDate());
            existing.setStatus(updated.getStatus() != null ? updated.getStatus() : existing.getStatus());
            milestoneDao.update(existing);
            return existing;
        } catch (SQLException e) {
            throw new ApiException(500, "Database error updating milestone: " + e.getMessage());
        }
    }

    public void deleteMilestone(int milestoneId, Role requesterRole) {
        requireProjectManager(requesterRole);
        try {
            boolean deleted = milestoneDao.delete(milestoneId);
            if (!deleted) throw new ApiException(404, "Milestone not found");
        } catch (SQLException e) {
            throw new ApiException(500, "Database error deleting milestone: " + e.getMessage());
        }
    }
}
