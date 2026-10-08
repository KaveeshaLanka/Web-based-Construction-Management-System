package com.lankabuild.cms.handler;

import com.lankabuild.cms.exception.ApiException;
import com.lankabuild.cms.model.Task;
import com.lankabuild.cms.model.TaskStatus;
import com.lankabuild.cms.service.TaskService;
import com.lankabuild.cms.util.HttpUtil;
import com.lankabuild.cms.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles all Task Assignment and Tracking endpoints:
 *   POST   /api/tasks
 *   GET    /api/tasks
 *   GET    /api/tasks/my-tasks           (Construction Worker's own tasks)
 *   GET    /api/tasks/project/{id}       (tasks for a specific project)
 *   GET    /api/tasks/{id}
 *   PUT    /api/tasks/{id}
 *   PATCH  /api/tasks/{id}/status        (worker updates progress)
 *   DELETE /api/tasks/{id}
 */
public class TaskHandler implements HttpHandler {

    private final TaskService taskService = new TaskService();
    private static final Pattern TASK_STATUS = Pattern.compile("^/api/tasks/(\\d+)/status/?$");
    private static final Pattern TASK_BY_ID = Pattern.compile("^/api/tasks/(\\d+)/?$");
    private static final Pattern TASKS_BY_PROJECT = Pattern.compile("^/api/tasks/project/(\\d+)/?$");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            AuthContext auth = AuthContext.fromRequest(exchange); // all task routes require login

            Matcher statusMatcher = TASK_STATUS.matcher(path);
            Matcher idMatcher = TASK_BY_ID.matcher(path);
            Matcher projectMatcher = TASKS_BY_PROJECT.matcher(path);

            if (path.equals("/api/tasks") && method.equals("POST")) {
                createTask(exchange, auth);
            } else if (path.equals("/api/tasks") && method.equals("GET")) {
                listTasks(exchange, auth);
            } else if (path.equals("/api/tasks/my-tasks") && method.equals("GET")) {
                myTasks(exchange, auth);
            } else if (projectMatcher.matches() && method.equals("GET")) {
                tasksByProject(exchange, auth, Integer.parseInt(projectMatcher.group(1)));
            } else if (statusMatcher.matches() && method.equals("PATCH")) {
                updateStatus(exchange, auth, Integer.parseInt(statusMatcher.group(1)));
            } else if (idMatcher.matches() && method.equals("GET")) {
                getTask(exchange, auth, Integer.parseInt(idMatcher.group(1)));
            } else if (idMatcher.matches() && method.equals("PUT")) {
                updateTask(exchange, auth, Integer.parseInt(idMatcher.group(1)));
            } else if (idMatcher.matches() && method.equals("DELETE")) {
                deleteTask(exchange, auth, Integer.parseInt(idMatcher.group(1)));
            } else {
                HttpUtil.sendError(exchange, 404, "Route not found");
            }
        } catch (ApiException e) {
            HttpUtil.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 500, "Unexpected server error: " + e.getMessage());
        }
    }

    private void createTask(HttpExchange exchange, AuthContext auth) throws IOException {
        String body = HttpUtil.readBody(exchange);
        Task task = JsonUtil.fromJson(body, Task.class);
        task.setCreatedBy(auth.userId);
        Task created = taskService.createTask(task, auth.role);
        HttpUtil.sendJson(exchange, 201, created);
    }

    private void listTasks(HttpExchange exchange, AuthContext auth) throws IOException {
        List<Task> tasks = taskService.getAllTasks(auth.role);
        HttpUtil.sendJson(exchange, 200, tasks);
    }

    private void myTasks(HttpExchange exchange, AuthContext auth) throws IOException {
        List<Task> tasks = taskService.getMyTasks(auth.userId);
        HttpUtil.sendJson(exchange, 200, tasks);
    }

    private void tasksByProject(HttpExchange exchange, AuthContext auth, int projectId) throws IOException {
        List<Task> tasks = taskService.getTasksByProject(projectId, auth.role);
        HttpUtil.sendJson(exchange, 200, tasks);
    }

    private void getTask(HttpExchange exchange, AuthContext auth, int id) throws IOException {
        Task task = taskService.getTaskById(id, auth.role, auth.userId);
        HttpUtil.sendJson(exchange, 200, task);
    }

    private void updateTask(HttpExchange exchange, AuthContext auth, int id) throws IOException {
        String body = HttpUtil.readBody(exchange);
        Task task = JsonUtil.fromJson(body, Task.class);
        Task updated = taskService.updateTask(id, task, auth.role);
        HttpUtil.sendJson(exchange, 200, updated);
    }

    private void updateStatus(HttpExchange exchange, AuthContext auth, int id) throws IOException {
        String body = HttpUtil.readBody(exchange);
        Map<?, ?> req = JsonUtil.fromJson(body, Map.class);
        String statusStr = (String) req.get("status");
        TaskStatus status;
        try {
            status = TaskStatus.valueOf(statusStr.toUpperCase());
        } catch (Exception e) {
            throw new ApiException(400, "Invalid status. Must be one of: PENDING, IN_PROGRESS, COMPLETED, DELAYED");
        }
        Task updated = taskService.updateTaskStatus(id, status, auth.role, auth.userId);
        HttpUtil.sendJson(exchange, 200, updated);
    }

    private void deleteTask(HttpExchange exchange, AuthContext auth, int id) throws IOException {
        taskService.deleteTask(id, auth.role);
        HttpUtil.sendJson(exchange, 200, Map.of("message", "Task deleted successfully"));
    }
}
