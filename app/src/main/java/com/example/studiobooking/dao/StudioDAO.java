package com.example.studiobooking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.studiobooking.model.Studio;

public class StudioDAO {

    public List<Studio> getAllStudios() {
        List<Studio> studios = new ArrayList<>();
        String sql = "SELECT * FROM studios";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                studios.add(new Studio(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) {
        }
        return studios;
    }

    public List<Studio> getActiveStudios() {
        List<Studio> studios = new ArrayList<>();
        String sql = "SELECT * FROM studios WHERE is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                studios.add(new Studio(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) {
        }
        return studios;
    }

    public List<Studio> getActiveStudios() {
        List<Studio> studios = new ArrayList<>();
        String sql = "SELECT * FROM studios WHERE is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                studios.add(new Studio(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studios;
    }

    public boolean updateStudioStatus(long studioId, boolean active) {
    String sql = "UPDATE studios SET is_active = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setBoolean(1, active);
        stmt.setLong(2, studioId);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

    public boolean addStudio(Studio studio) {
        String sql = "INSERT INTO studios (name, description, is_active) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studio.getName());
            stmt.setString(2, studio.getDescription());
            stmt.setBoolean(3, studio.isActive());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateStudio(Studio studio) {
        String sql = "UPDATE studios SET name = ?, description = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studio.getName());
            stmt.setString(2, studio.getDescription());
            stmt.setBoolean(3, studio.isActive());
            stmt.setLong(4, studio.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean deleteStudio(long id) {
        String sql = "DELETE FROM studios WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public Studio getStudioById(long id) {
        String sql = "SELECT * FROM studios WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Studio(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getBoolean("is_active")
                    );
                }
            }
        } catch (SQLException e) {
        }
        return null;
    }
}