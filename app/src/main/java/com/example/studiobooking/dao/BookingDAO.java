package com.example.studiobooking.dao;

import com.example.studiobooking.model.Booking;
import com.example.studiobooking.model.Equipment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    // Controlla se lo studio è disponibile in un intervallo
    public boolean isAvailable(long studioId, LocalDateTime start, LocalDateTime end) {
        String sql = """
            SELECT COUNT(*) AS count
            FROM bookings
            WHERE studio_id = ? 
              AND status = 'CONFIRMED'
              AND ((start_time < ? AND end_time > ?) 
                   OR (start_time < ? AND end_time > ?) 
                   OR (start_time >= ? AND end_time <= ?))
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studioId);
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            stmt.setTimestamp(3, Timestamp.valueOf(end));
            stmt.setTimestamp(4, Timestamp.valueOf(start));
            stmt.setTimestamp(5, Timestamp.valueOf(start));
            stmt.setTimestamp(6, Timestamp.valueOf(start));
            stmt.setTimestamp(7, Timestamp.valueOf(end));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") == 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Recupera le prenotazioni di un utente
    public List<Booking> getBookingsByUser(long userId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.id, b.user_id, u.name AS user_name, b.studio_id, " +
                     "b.start_time, b.end_time, b.status " +
                     "FROM bookings b JOIN users u ON b.user_id = u.id " +
                     "WHERE b.user_id = ? ORDER BY b.start_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

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

    // Crea una prenotazione e associa l'equipment selezionato
    public boolean createBooking(long userId, long studioId, LocalDateTime start, LocalDateTime end, List<Equipment> equipmentList) {
        String insertBooking = "INSERT INTO bookings (user_id, studio_id, start_time, end_time, status) VALUES (?, ?, ?, ?, 'CONFIRMED')";
        String insertEquipment = "INSERT INTO booking_equipment (booking_id, equipment_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            long bookingId;
            try (PreparedStatement stmt = conn.prepareStatement(insertBooking, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, userId);
                stmt.setLong(2, studioId);
                stmt.setTimestamp(3, Timestamp.valueOf(start));
                stmt.setTimestamp(4, Timestamp.valueOf(end));
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    bookingId = rs.getLong(1);
                } else {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertEquipment)) {
                for (Equipment e : equipmentList) {
                    stmt.setLong(1, bookingId);
                    stmt.setLong(2, e.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cancella una prenotazione se mancano almeno 24 ore all'inizio
    public boolean cancelBooking(long bookingId) {
        String sql = "UPDATE bookings SET status = 'CANCELLED' " +
                     "WHERE id = ? AND start_time > NOW() + INTERVAL 24 HOUR AND status = 'CONFIRMED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, bookingId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Nuovo metodo: conta le prenotazioni confermate di un utente (per loyalty card)
    public int countBookingsByUser(long userId) {
        int count = 0;
        String sql = "SELECT COUNT(*) AS total FROM bookings WHERE user_id = ? AND status != 'CANCELLED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }
}
