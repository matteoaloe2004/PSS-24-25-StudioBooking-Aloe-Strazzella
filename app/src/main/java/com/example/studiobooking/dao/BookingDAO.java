package com.example.studiobooking.dao;

import com.example.studiobooking.model.Booking;
import com.example.studiobooking.model.Equipment;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    // Controlla conflitti di orario per uno studio
    public boolean hasConflict(long studioId, LocalDateTime start, LocalDateTime end, Long excludeBookingId) {
        String sql = "SELECT COUNT(*) FROM bookings WHERE studio_id = ? " +
                     "AND ((start_time < ? AND end_time > ?) OR " +
                     "(start_time < ? AND end_time > ?) OR " +
                     "(start_time >= ? AND end_time <= ?))";
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

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Errore nel controllo dei conflitti per studioId=" + studioId);
            e.printStackTrace();
        }
        return true; // default: assume conflitto
    }

    public boolean isAvailable(long studioId, LocalDateTime start, LocalDateTime end) {
        return !hasConflict(studioId, start, end, null);
    }

    // Crea una nuova prenotazione
    public boolean createBooking(long userId, long studioId, LocalDateTime start, LocalDateTime end, List<Equipment> equipmentList) {
        if (hasConflict(studioId, start, end, null)) return false;

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

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) bookingId = rs.getLong(1);
                    else throw new SQLException("Booking ID non generato");
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertEquipment)) {
                for (Equipment eq : equipmentList) {
                    stmt.setLong(1, bookingId);
                    stmt.setLong(2, eq.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            conn.commit();

            // Aggiorna loyalty card
            LoyaltyCardDAO loyaltyDAO = new LoyaltyCardDAO();
            loyaltyDAO.refreshLoyaltyCard(userId);

            return true;

        } catch (SQLException e) {
            System.err.println("Errore nella creazione della prenotazione per userId=" + userId);
            e.printStackTrace();
        }
        return false;
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                bookings.add(new Booking(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        "", // userName vuoto, da popolare se necessario
                        rs.getLong("studio_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getString("status")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public List<Booking> getBookingsByUser(long userId) {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(new Booking(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            "", // userName vuoto
                            rs.getLong("studio_id"),
                            rs.getTimestamp("start_time").toLocalDateTime(),
                            rs.getTimestamp("end_time").toLocalDateTime(),
                            rs.getString("status")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }

    public boolean updateBooking(long bookingId, long studioId, LocalDateTime start, LocalDateTime end, String status) {
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
        }
        return false;
    }

    public boolean deleteBooking(long bookingId) {
        String sql = "DELETE FROM bookings WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, bookingId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelBooking(long bookingId) {
        String sql = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, bookingId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
