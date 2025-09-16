package com.example.studiobooking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.studiobooking.model.Booking;
import com.example.studiobooking.model.Equipment;
import com.example.studiobooking.model.Studio;

public class AdminDAO {

    // CREA UN NUOVO ADMIN
    public boolean createAdmin(String name, String email, String hashedPassword) {
        String sql = "INSERT INTO users (name, email, password_hash, created_at, is_admin) VALUES (?, ?, ?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, hashedPassword);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // OTTIENE TUTTI GLI STUDI
    public List<Studio> getAllStudios() {
        List<Studio> studios = new ArrayList<>();
        String sql = "SELECT * FROM studios ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Studio s = new Studio(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBoolean("is_active")
                );
                studios.add(s);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studios;
    }

    // ABBILITA O DISABILITA UNO STUDIO
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

    // OTTIENE TUTTE LE PRENOTAZIONI
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.id, b.user_id, u.name AS user_name, b.studio_id,
                   b.start_time, b.end_time, b.status
            FROM bookings b
            JOIN users u ON b.user_id = u.id
            ORDER BY b.start_time DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Booking b = new Booking(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("user_name"),
                        rs.getLong("studio_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getString("status")
                );
                bookings.add(b);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    // CANCELLA UNA PRENOTAZIONE
    public boolean deleteBooking(long bookingId) {
        String sql = "DELETE FROM bookings WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, bookingId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // CONTROLLO CONFLITTO
    public boolean isBookingConflict(long studioId, LocalDateTime start, LocalDateTime end, Long excludeBookingId) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE studio_id = ? AND " +
                     "((start_time < ? AND end_time > ?) OR (start_time < ? AND end_time > ?) OR (start_time >= ? AND end_time <= ?))";
        if (excludeBookingId != null) sql += " AND id <> ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studioId);
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            stmt.setTimestamp(3, Timestamp.valueOf(start));
            stmt.setTimestamp(4, Timestamp.valueOf(end));
            stmt.setTimestamp(5, Timestamp.valueOf(start));
            stmt.setTimestamp(6, Timestamp.valueOf(start));
            stmt.setTimestamp(7, Timestamp.valueOf(end));

            if (excludeBookingId != null) stmt.setLong(8, excludeBookingId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    // AGGIUNGI PRENOTAZIONE
    public boolean addBooking(long userId, long studioId, LocalDateTime start, LocalDateTime end, String status) {
        if (isBookingConflict(studioId, start, end, null)) return false;
        String sql = "INSERT INTO bookings (user_id, studio_id, start_time, end_time, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setLong(2, studioId);
            stmt.setTimestamp(3, Timestamp.valueOf(start));
            stmt.setTimestamp(4, Timestamp.valueOf(end));
            stmt.setString(5, status);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // MODIFICA PRENOTAZIONE
    public boolean updateBooking(long bookingId, long studioId, LocalDateTime start, LocalDateTime end, String status) {
        if (isBookingConflict(studioId, start, end, bookingId)) return false;
        String sql = "UPDATE bookings SET studio_id = ?, start_time = ?, end_time = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studioId);
            stmt.setTimestamp(2, Timestamp.valueOf(start));
            stmt.setTimestamp(3, Timestamp.valueOf(end));
            stmt.setString(4, status);
            stmt.setLong(5, bookingId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==========================
    // GESTIONE EQUIPMENT
    // ==========================

    // OTTIENE TUTTA L'EQUIP.
    public List<Equipment> getAllEquipment() {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT * FROM equipment ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Equipment eq = new Equipment(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getBoolean("is_available")
                );
                list.add(eq);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // AGGIUNGI NUOVA ATTREZZATURA
    public long addEquipment(String name, String description) {
        String sql = "INSERT INTO equipment (name, description, is_available) VALUES (?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) return keys.getLong(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    // ASSEGNA ATTREZZATURA A STUDI
    public void assignEquipmentToStudios(long equipmentId, List<Long> studioIds) {
    String sql = "INSERT INTO studio_equipment (studio_id, equipment_id) VALUES (?, ?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        for (Long id : studioIds) {
            stmt.setLong(1, id);          // studio_id
            stmt.setLong(2, equipmentId); // equipment_id
            stmt.addBatch();
        }
        stmt.executeBatch();
    } catch (SQLException e) { e.printStackTrace(); }
}

    // ATTIVA/DISATTIVA ATTREZZATURA
    public boolean toggleEquipment(long equipmentId) {
        String sql = "UPDATE equipment SET is_available = NOT is_available WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, equipmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ELIMINA ATTREZZATURA
    public boolean deleteEquipment(long equipmentId) {
        String sql = "DELETE FROM equipment WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, equipmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
