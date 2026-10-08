package com.lankabuild.cms.dao;

import com.lankabuild.cms.config.DBConfig;
import com.lankabuild.cms.model.Milestone;
import com.lankabuild.cms.model.MilestoneStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MilestoneDao {

    public Milestone create(Milestone m) throws SQLException {
        String sql = "INSERT INTO milestones (project_id, title, description, due_date, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getProjectId());
            ps.setString(2, m.getTitle());
            ps.setString(3, m.getDescription());
            ps.setDate(4, m.getDueDate() != null ? Date.valueOf(m.getDueDate()) : null);
            ps.setString(5, m.getStatus() != null ? m.getStatus().name() : MilestoneStatus.PENDING.name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) m.setId(rs.getInt(1));
            }
        }
        return m;
    }

    public List<Milestone> findByProjectId(int projectId) throws SQLException {
        List<Milestone> list = new ArrayList<>();
        String sql = "SELECT * FROM milestones WHERE project_id = ? ORDER BY due_date ASC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<Milestone> findById(int id) throws SQLException {
        String sql = "SELECT * FROM milestones WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public boolean update(Milestone m) throws SQLException {
        String sql = "UPDATE milestones SET title=?, description=?, due_date=?, status=? WHERE id=?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getDescription());
            ps.setDate(3, m.getDueDate() != null ? Date.valueOf(m.getDueDate()) : null);
            ps.setString(4, m.getStatus().name());
            ps.setInt(5, m.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM milestones WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Milestone mapRow(ResultSet rs) throws SQLException {
        Milestone m = new Milestone();
        m.setId(rs.getInt("id"));
        m.setProjectId(rs.getInt("project_id"));
        m.setTitle(rs.getString("title"));
        m.setDescription(rs.getString("description"));
        Date due = rs.getDate("due_date");
        if (due != null) m.setDueDate(due.toLocalDate());
        m.setStatus(MilestoneStatus.valueOf(rs.getString("status")));
        return m;
    }
}
