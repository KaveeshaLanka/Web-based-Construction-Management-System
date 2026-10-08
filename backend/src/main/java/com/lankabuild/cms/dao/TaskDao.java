package com.lankabuild.cms.dao;

import com.lankabuild.cms.config.DBConfig;
import com.lankabuild.cms.model.Task;
import com.lankabuild.cms.model.TaskStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskDao {

    public Task create(Task t) throws SQLException {
        String sql = "INSERT INTO tasks (project_id, title, description, assigned_to, deadline, status, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getProjectId());
            ps.setString(2, t.getTitle());
            ps.setString(3, t.getDescription());
            if (t.getAssignedTo() != null) ps.setInt(4, t.getAssignedTo()); else ps.setNull(4, Types.INTEGER);
            ps.setDate(5, t.getDeadline() != null ? Date.valueOf(t.getDeadline()) : null);
            ps.setString(6, t.getStatus() != null ? t.getStatus().name() : TaskStatus.PENDING.name());
            ps.setInt(7, t.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.setId(rs.getInt(1));
            }
        }
        return findById(t.getId()).orElse(t);
    }

    public Optional<Task> findById(int id) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<Task> findAll() throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM tasks ORDER BY created_at DESC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Task> findByProjectId(int projectId) throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE project_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Task> findByAssignedTo(int userId) throws SQLException {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE assigned_to = ? ORDER BY deadline ASC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean update(Task t) throws SQLException {
        String sql = "UPDATE tasks SET title=?, description=?, assigned_to=?, deadline=?, status=? WHERE id=?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTitle());
            ps.setString(2, t.getDescription());
            if (t.getAssignedTo() != null) ps.setInt(3, t.getAssignedTo()); else ps.setNull(3, Types.INTEGER);
            ps.setDate(4, t.getDeadline() != null ? Date.valueOf(t.getDeadline()) : null);
            ps.setString(5, t.getStatus().name());
            ps.setInt(6, t.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, TaskStatus status) throws SQLException {
        String sql = "UPDATE tasks SET status=? WHERE id=?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id"));
        t.setProjectId(rs.getInt("project_id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        int assignedTo = rs.getInt("assigned_to");
        t.setAssignedTo(rs.wasNull() ? null : assignedTo);
        Date deadline = rs.getDate("deadline");
        if (deadline != null) t.setDeadline(deadline.toLocalDate());
        t.setStatus(TaskStatus.valueOf(rs.getString("status")));
        t.setCreatedBy(rs.getInt("created_by"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) t.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) t.setUpdatedAt(updatedAt.toLocalDateTime());
        return t;
    }
}
