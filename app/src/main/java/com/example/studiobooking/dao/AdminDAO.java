package com.example.studiobooking.dao;

import com.example.studiobooking.model.Studio;
import com.example.studiobooking.model.Booking;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    // CREA UN NUOVO ADMIN
    public boolean createAdmin(String name, String email, String hashedPassword) {
        String sql = "INSERT INTO users (name, email, password, created_at, is_admin) VALUES (?, ?, ?, ?, TRUE)";
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

    // OTTIENE TUTTE LE PRENOTAZIONI CON NOME UTENTE
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

    // MODIFICA UNA PRENOTAZIONE
    public boolean updateBookingStatus(long bookingId, String newStatus) {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setLong(2, bookingId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
}
