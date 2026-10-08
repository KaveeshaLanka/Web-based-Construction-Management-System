package com.lankabuild.cms.service;

import com.lankabuild.cms.dao.UserDao;
import com.lankabuild.cms.exception.ApiException;
import com.lankabuild.cms.model.Role;
import com.lankabuild.cms.model.User;

import java.sql.SQLException;
import java.util.List;

public class UserService {

    private final UserDao userDao = new UserDao();

    public List<User> getUsers(Role filterRole) {
        try {
            List<User> users = (filterRole != null)
                    ? userDao.findByRole(filterRole)
                    : userDao.findAll();
            users.forEach(u -> u.setPasswordHash(null));
            return users;
        } catch (SQLException e) {
            throw new ApiException(500, "Database error fetching users: " + e.getMessage());
        }
    }
}