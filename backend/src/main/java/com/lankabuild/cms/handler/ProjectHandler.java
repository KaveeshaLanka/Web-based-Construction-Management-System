package com.lankabuild.cms.handler;

import com.lankabuild.cms.exception.ApiException;
import com.lankabuild.cms.model.Milestone;
import com.lankabuild.cms.model.Project;
import com.lankabuild.cms.service.ProjectService;
import com.lankabuild.cms.util.HttpUtil;
import com.lankabuild.cms.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles all Project Management endpoints (CRUD + milestones):
 *   POST   /api/projects
 *   GET    /api/projects
 *   GET    /api/projects/{id}
 *   PUT    /api/projects/{id}
 *   DELETE /api/projects/{id}
 *   POST   /api/projects/{id}/milestones
 *   GET    /api/projects/{id}/milestones
 *   PUT    /api/milestones/{id}
 *   DELETE /api/milestones/{id}
 */
public class ProjectHandler implements HttpHandler {

    private final ProjectService projectService = new ProjectService();
    private static final Pattern PROJECT_MILESTONES = Pattern.compile("^/api/projects/(\\d+)/milestones/?$");
    private static final Pattern PROJECT_BY_ID = Pattern.compile("^/api/projects/(\\d+)/?$");
    private static final Pattern MILESTONE_BY_ID = Pattern.compile("^/api/milestones/(\\d+)/?$");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            AuthContext auth = AuthContext.fromRequest(exchange); // all project routes require login

            Matcher milestonesMatcher = PROJECT_MILESTONES.matcher(path);
            Matcher projectIdMatcher = PROJECT_BY_ID.matcher(path);
            Matcher milestoneIdMatcher = MILESTONE_BY_ID.matcher(path);

            if (path.equals("/api/projects") && method.equals("POST")) {
                createProject(exchange, auth);
            } else if (path.equals("/api/projects") && method.equals("GET")) {
                listProjects(exchange, auth);
            } else if (milestonesMatcher.matches() && method.equals("POST")) {
                addMilestone(exchange, auth, Integer.parseInt(milestonesMatcher.group(1)));
            } else if (milestonesMatcher.matches() && method.equals("GET")) {
                listMilestones(exchange, auth, Integer.parseInt(milestonesMatcher.group(1)));
            } else if (milestoneIdMatcher.matches() && method.equals("PUT")) {
                updateMilestone(exchange, auth, Integer.parseInt(milestoneIdMatcher.group(1)));
            } else if (milestoneIdMatcher.matches() && method.equals("DELETE")) {
                deleteMilestone(exchange, auth, Integer.parseInt(milestoneIdMatcher.group(1)));
            } else if (projectIdMatcher.matches() && method.equals("GET")) {
                getProject(exchange, auth, Integer.parseInt(projectIdMatcher.group(1)));
            } else if (projectIdMatcher.matches() && method.equals("PUT")) {
                updateProject(exchange, auth, Integer.parseInt(projectIdMatcher.group(1)));
            } else if (projectIdMatcher.matches() && method.equals("DELETE")) {
                deleteProject(exchange, auth, Integer.parseInt(projectIdMatcher.group(1)));
            } else {
                HttpUtil.sendError(exchange, 404, "Route not found");
            }
        } catch (ApiException e) {
            HttpUtil.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 500, "Unexpected server error: " + e.getMessage());
        }
    }

    private void createProject(HttpExchange exchange, AuthContext auth) throws IOException {
        String body = HttpUtil.readBody(exchange);
        Project project = JsonUtil.fromJson(body, Project.class);
        Project created = projectService.createProject(project, auth.role, auth.userId);
        HttpUtil.sendJson(exchange, 201, created);
    }

    private void listProjects(HttpExchange exchange, AuthContext auth) throws IOException {
        List<Project> projects = projectService.getAllProjects(auth.role);
        HttpUtil.sendJson(exchange, 200, projects);
    }

    private void getProject(HttpExchange exchange, AuthContext auth, int id) throws IOException {
        Project project = projectService.getProjectById(id, auth.role);
        HttpUtil.sendJson(exchange, 200, project);
    }

    private void updateProject(HttpExchange exchange, AuthContext auth, int id) throws IOException {
        String body = HttpUtil.readBody(exchange);
        Project project = JsonUtil.fromJson(body, Project.class);
        Project updated = projectService.updateProject(id, project, auth.role);
        HttpUtil.sendJson(exchange, 200, updated);
    }

    private void deleteProject(HttpExchange exchange, AuthContext auth, int id) throws IOException {
        projectService.deleteProject(id, auth.role);
        HttpUtil.sendJson(exchange, 200, java.util.Map.of("message", "Project deleted successfully"));
    }

    private void addMilestone(HttpExchange exchange, AuthContext auth, int projectId) throws IOException {
        String body = HttpUtil.readBody(exchange);
        Milestone milestone = JsonUtil.fromJson(body, Milestone.class);
        Milestone created = projectService.addMilestone(projectId, milestone, auth.role);
        HttpUtil.sendJson(exchange, 201, created);
    }

    private void listMilestones(HttpExchange exchange, AuthContext auth, int projectId) throws IOException {
        List<Milestone> milestones = projectService.getMilestones(projectId, auth.role);
        HttpUtil.sendJson(exchange, 200, milestones);
    }

    private void updateMilestone(HttpExchange exchange, AuthContext auth, int milestoneId) throws IOException {
        String body = HttpUtil.readBody(exchange);
        Milestone milestone = JsonUtil.fromJson(body, Milestone.class);
        Milestone updated = projectService.updateMilestone(milestoneId, milestone, auth.role);
        HttpUtil.sendJson(exchange, 200, updated);
    }

    private void deleteMilestone(HttpExchange exchange, AuthContext auth, int milestoneId) throws IOException {
        projectService.deleteMilestone(milestoneId, auth.role);
        HttpUtil.sendJson(exchange, 200, java.util.Map.of("message", "Milestone deleted successfully"));
    }
}
