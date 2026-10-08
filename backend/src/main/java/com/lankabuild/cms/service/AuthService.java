package com.lankabuild.cms.service;

import com.lankabuild.cms.dao.UserDao;
import com.lankabuild.cms.exception.ApiException;
import com.lankabuild.cms.model.Role;
import com.lankabuild.cms.model.User;
import com.lankabuild.cms.util.JwtUtil;
import com.lankabuild.cms.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDao userDao = new UserDao();

    public User signup(String fullName, String email, String plainPassword, String roleStr) {
        try {
            if (fullName == null || fullName.isBlank() || email == null || email.isBlank() || plainPassword == null || plainPassword.length() < 6) {
                throw new ApiException(400, "fullName, email are required and password must be at least 6 characters");
            }
            Role role;
            try {
                role = Role.valueOf(roleStr.toUpperCase());
            } catch (Exception e) {
                throw new ApiException(400, "Invalid role. Must be one of: OPERATIONS_MANAGER, PROJECT_MANAGER, SITE_SUPERVISOR, FINANCE_OFFICER, PROCUREMENT_OFFICER, CONSTRUCTION_WORKER");
            }

            Optional<User> existing = userDao.findByEmail(email);
            if (existing.isPresent()) {
                throw new ApiException(409, "An account with this email already exists");
            }

            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPasswordHash(PasswordUtil.hash(plainPassword));
            user.setRole(role);

            User saved = userDao.create(user);
            saved.setPasswordHash(null); // never return the hash
            return saved;
        } catch (SQLException e) {
            throw new ApiException(500, "Database error during signup: " + e.getMessage());
        }
    }

    /** Returns a JWT token string on success. */
    public String login(String email, String plainPassword) {
        try {
            User user = userDao.findByEmail(email)
                    .orElseThrow(() -> new ApiException(401, "Invalid email or password"));

            if (!PasswordUtil.verify(plainPassword, user.getPasswordHash())) {
                throw new ApiException(401, "Invalid email or password");
            }

            return JwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        } catch (SQLException e) {
            throw new ApiException(500, "Database error during login: " + e.getMessage());
        }
    }
}
