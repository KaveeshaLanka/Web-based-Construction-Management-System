package com.lankabuild.cms.dao;

import com.lankabuild.cms.config.DBConfig;
import com.lankabuild.cms.model.Project;
import com.lankabuild.cms.model.ProjectStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectDao {

    public Project create(Project p) throws SQLException {
        String sql = "INSERT INTO projects (name, description, client_name, location, start_date, end_date, budget, status, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getClientName());
            ps.setString(4, p.getLocation());
            ps.setDate(5, p.getStartDate() != null ? Date.valueOf(p.getStartDate()) : null);
            ps.setDate(6, p.getEndDate() != null ? Date.valueOf(p.getEndDate()) : null);
            ps.setBigDecimal(7, p.getBudget());
            ps.setString(8, p.getStatus() != null ? p.getStatus().name() : ProjectStatus.PLANNING.name());
            ps.setInt(9, p.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        }
        return findById(p.getId()).orElse(p);
    }

    public Optional<Project> findById(int id) throws SQLException {
        String sql = "SELECT * FROM projects WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<Project> findAll() throws SQLException {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM projects ORDER BY created_at DESC";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean update(Project p) throws SQLException {
        String sql = "UPDATE projects SET name=?, description=?, client_name=?, location=?, start_date=?, end_date=?, budget=?, status=? WHERE id=?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getClientName());
            ps.setString(4, p.getLocation());
            ps.setDate(5, p.getStartDate() != null ? Date.valueOf(p.getStartDate()) : null);
            ps.setDate(6, p.getEndDate() != null ? Date.valueOf(p.getEndDate()) : null);
            ps.setBigDecimal(7, p.getBudget());
            ps.setString(8, p.getStatus().name());
            ps.setInt(9, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM projects WHERE id = ?";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Project mapRow(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setClientName(rs.getString("client_name"));
        p.setLocation(rs.getString("location"));
        Date sd = rs.getDate("start_date");
        if (sd != null) p.setStartDate(sd.toLocalDate());
        Date ed = rs.getDate("end_date");
        if (ed != null) p.setEndDate(ed.toLocalDate());
        p.setBudget(rs.getBigDecimal("budget"));
        p.setStatus(ProjectStatus.valueOf(rs.getString("status")));
        p.setCreatedBy(rs.getInt("created_by"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) p.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) p.setUpdatedAt(updatedAt.toLocalDateTime());
        return p;
    }
}
