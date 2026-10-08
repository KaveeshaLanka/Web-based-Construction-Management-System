package com.lankabuild.cms.handler;

import com.lankabuild.cms.exception.ApiException;
import com.lankabuild.cms.model.Role;
import com.lankabuild.cms.util.HttpUtil;
import com.lankabuild.cms.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;

/** Extracts and validates the logged-in user's identity from the Authorization header. */
public class AuthContext {

    public final int userId;
    public final String email;
    public final Role role;

    private AuthContext(int userId, String email, Role role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public static AuthContext fromRequest(HttpExchange exchange) {
        String token = HttpUtil.getBearerToken(exchange);
        if (token == null) {
            throw new ApiException(401, "Missing or invalid Authorization header. Expected: Bearer <token>");
        }
        try {
            Claims claims = JwtUtil.validateAndGetClaims(token);
            int userId = Integer.parseInt(claims.getSubject());
            String email = claims.get("email", String.class);
            Role role = Role.valueOf(claims.get("role", String.class));
            return new AuthContext(userId, email, role);
        } catch (Exception e) {
            throw new ApiException(401, "Invalid or expired token");
        }
    }
}
