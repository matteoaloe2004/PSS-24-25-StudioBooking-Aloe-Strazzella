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

public class BookingDAO {

    // Controlla se ci sono conflitti di prenotazione
    public boolean hasConflict(long studioId, LocalDateTime start, LocalDateTime end, Long excludeBookingId) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE studio_id = ? "
                   + "AND ((start_time < ? AND end_time > ?) OR "
                   + "(start_time < ? AND end_time > ?) OR "
                   + "(start_time >= ? AND end_time <= ?))";
        if (excludeBookingId != null) {
            sql += " AND id <> ?";
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studioId);
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            stmt.setTimestamp(3, Timestamp.valueOf(start));
            stmt.setTimestamp(4, Timestamp.valueOf(end));
            stmt.setTimestamp(5, Timestamp.valueOf(start));
            stmt.setTimestamp(6, Timestamp.valueOf(start));
            stmt.setTimestamp(7, Timestamp.valueOf(end));
            if (excludeBookingId != null) {
                stmt.setLong(8, excludeBookingId);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    public boolean isAvailable(long studioId, LocalDateTime start, LocalDateTime end) {
        return !hasConflict(studioId, start, end, null);
    }

    // Recupera prenotazioni di un utente
    public List<Booking> getBookingsByUser(long userId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = """
            SELECT b.id, b.user_id, u.name AS user_name, b.studio_id,
                   b.start_time, b.end_time, b.status
            FROM bookings b
            JOIN users u ON b.user_id = u.id
            WHERE b.user_id = ?
            ORDER BY b.start_time DESC
        """;

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
            }
        } catch (SQLException e) {
        }
        return null;
    }

    // Crea una prenotazione
    public boolean createBooking(long userId, long studioId, LocalDateTime start,
                                 LocalDateTime end, List<Equipment> equipmentList) {
        if (hasConflict(studioId, start, end, null)) return false;

        String insertBooking = "INSERT INTO bookings (user_id, studio_id, start_time, end_time, status) "
                             + "VALUES (?, ?, ?, ?, 'CONFIRMED')";
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

            // Aggiorna loyalty card in base alle prenotazioni effettive (non CANCELLED)
            LoyaltyCardDAO loyaltyCardDAO = new LoyaltyCardDAO();
            loyaltyCardDAO.refreshLoyaltyCard(userId);

            conn.commit();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    // Cancella una prenotazione (se almeno 24h prima dell'inizio)
    public boolean cancelBooking(long bookingId) {
        String sql = "UPDATE bookings SET status = 'CANCELLED' " +
                     "WHERE id = ? AND start_time > NOW() + INTERVAL 24 HOUR AND status = 'CONFIRMED'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
