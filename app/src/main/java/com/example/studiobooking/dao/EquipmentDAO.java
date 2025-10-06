package com.example.studiobooking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.studiobooking.model.Equipment;

public class EquipmentDAO {

    public List<Equipment> getEquipmentByStudio(long studioId) {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT e.* FROM equipment e " +
                "JOIN studio_equipment se ON e.id = se.equipment_id " +
                "WHERE se.studio_id = ? AND e.is_available = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studioId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                equipmentList.add(new Equipment(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("is_available"),
                        rs.getString("type")
                ));
            }
        } catch (SQLException e) {
        }
        return equipmentList;
    }
}
