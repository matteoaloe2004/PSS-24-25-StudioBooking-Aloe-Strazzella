package com.example.studiobooking.dao;

import com.example.studiobooking.model.Equipment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                Equipment eq = new Equipment(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("is_available")
                );
                equipmentList.add(eq);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return equipmentList;
    }
}
