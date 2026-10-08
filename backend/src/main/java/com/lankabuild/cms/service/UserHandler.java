package com.lankabuild.cms.handler;

import com.lankabuild.cms.exception.ApiException;
import com.lankabuild.cms.model.Role;
import com.lankabuild.cms.model.User;
import com.lankabuild.cms.service.UserService;
import com.lankabuild.cms.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Handles: GET /api/users  (optional ?role=CONSTRUCTION_WORKER filter) */
public class UserHandler implements HttpHandler {

    private final UserService userService = new UserService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            AuthContext.fromRequest(exchange); // must be logged in

            if (!exchange.getRequestURI().getPath().equals("/api/users")
                    || !exchange.getRequestMethod().equals("GET")) {
                HttpUtil.sendError(exchange, 404, "Route not found");
                return;
            }

            String query = exchange.getRequestURI().getQuery(); // e.g. role=CONSTRUCTION_WORKER
            Role filterRole = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] kv = param.split("=");
                    if (kv.length == 2 && kv[0].equals("role")) {
                        try {
                            filterRole = Role.valueOf(kv[1].toUpperCase());
                        } catch (Exception e) {
                            throw new ApiException(400, "Invalid role filter");
                        }
                    }
                }
            }

            List<User> users = userService.getUsers(filterRole);
            HttpUtil.sendJson(exchange, 200, Map.of("users", users));
        } catch (ApiException e) {
            HttpUtil.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 500, "Unexpected server error: " + e.getMessage());
        }
    }
}