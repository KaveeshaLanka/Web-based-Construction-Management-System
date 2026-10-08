package com.lankabuild.cms.handler;

import com.lankabuild.cms.exception.ApiException;
import com.lankabuild.cms.model.User;
import com.lankabuild.cms.service.AuthService;
import com.lankabuild.cms.util.HttpUtil;
import com.lankabuild.cms.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles:
 *   POST /api/auth/signup
 *   POST /api/auth/login
 */
public class AuthHandler implements HttpHandler {

    private final AuthService authService = new AuthService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (path.equals("/api/auth/signup") && method.equals("POST")) {
                handleSignup(exchange);
            } else if (path.equals("/api/auth/login") && method.equals("POST")) {
                handleLogin(exchange);
            } else {
                HttpUtil.sendError(exchange, 404, "Route not found");
            }
        } catch (ApiException e) {
            HttpUtil.sendError(exchange, e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 500, "Unexpected server error: " + e.getMessage());
        }
    }

    private void handleSignup(HttpExchange exchange) throws IOException {
        String body = HttpUtil.readBody(exchange);
        Map<?, ?> req = JsonUtil.fromJson(body, Map.class);
        String fullName = (String) req.get("fullName");
        String email = (String) req.get("email");
        String password = (String) req.get("password");
        String role = (String) req.get("role");

        User created = authService.signup(fullName, email, password, role);
        HttpUtil.sendJson(exchange, 201, created);
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = HttpUtil.readBody(exchange);
        Map<?, ?> req = JsonUtil.fromJson(body, Map.class);
        String email = (String) req.get("email");
        String password = (String) req.get("password");

        String token = authService.login(email, password);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        HttpUtil.sendJson(exchange, 200, response);
    }
}
