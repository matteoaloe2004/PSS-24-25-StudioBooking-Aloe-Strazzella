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
        }
        return true;
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
                bookings.add(new Booking(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("user_name"),
                        rs.getLong("studio_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
        }
        return bookings;
    }

    // Recupera una singola prenotazione per ID
    public Booking getBookingById(long bookingId) {
        String sql = "SELECT b.id, b.user_id, u.name AS user_name, b.studio_id, "
                   + "b.start_time, b.end_time, b.status "
                   + "FROM bookings b JOIN users u ON b.user_id = u.id WHERE b.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookingId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Booking(
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

    public boolean isEquipmentAvailable(Equipment equipment, LocalDateTime start, LocalDateTime end) {
    String sql = """
        SELECT COUNT(*) 
        FROM booking_equipment be
        JOIN bookings b ON be.booking_id = b.id
        WHERE be.equipment_id = ? 
          AND b.status = 'CONFIRMED'
          AND ((b.start_time < ? AND b.end_time > ?) OR
               (b.start_time < ? AND b.end_time > ?) OR
               (b.start_time >= ? AND b.end_time <= ?))
    """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setLong(1, equipment.getId());
        stmt.setTimestamp(2, Timestamp.valueOf(end));
        stmt.setTimestamp(3, Timestamp.valueOf(start));
        stmt.setTimestamp(4, Timestamp.valueOf(end));
        stmt.setTimestamp(5, Timestamp.valueOf(start));
        stmt.setTimestamp(6, Timestamp.valueOf(start));
        stmt.setTimestamp(7, Timestamp.valueOf(end));

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) == 0; // true se non ci sono conflitti
        }
    } catch (SQLException e) {
    }
    return false;
}


    // Aggiorna prenotazione
    public boolean updateBooking(long bookingId, long studioId, LocalDateTime start,
                                 LocalDateTime end, String status) {
        if (hasConflict(studioId, start, end, bookingId)) return false;

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
            return false;
        }
    }

    // Cancella una prenotazione (se almeno 24h prima dell'inizio)
    public boolean cancelBooking(long bookingId) {
        Booking booking = getBookingById(bookingId);
        if (booking == null) return false;

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (booking.getStartTime().isAfter(now.plusHours(24))) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?"
                );
                stmt.setLong(1, bookingId);
                int updated = stmt.executeUpdate();

                if (updated > 0) {
                    // Aggiorna loyalty card in base alle prenotazioni effettive
                    LoyaltyCardDAO loyaltyCardDAO = new LoyaltyCardDAO();
                    loyaltyCardDAO.refreshLoyaltyCard(booking.getUserId());
                    return true;
                }
            } catch (SQLException e) {
            }
        }
        return false;
    }

    // Eliminazione definitiva prenotazione (solo admin)
    public boolean deleteBooking(long bookingId) {
        String sql = "DELETE FROM bookings WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookingId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

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
                bookings.add(new Booking(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("user_name"),
                        rs.getLong("studio_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
        }
        return bookings;
    }

    public List<Booking> getBookingsByStudio(long studioId) {
    List<Booking> bookings = new ArrayList<>();
    String sql = """
        SELECT b.id, b.user_id, u.name AS user_name, b.studio_id,
               b.start_time, b.end_time, b.status
        FROM bookings b
        JOIN users u ON b.user_id = u.id
        WHERE b.studio_id = ?
        ORDER BY b.start_time
    """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setLong(1, studioId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            bookings.add(new Booking(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("user_name"),
                    rs.getLong("studio_id"),
                    rs.getTimestamp("start_time").toLocalDateTime(),
                    rs.getTimestamp("end_time").toLocalDateTime(),
                    rs.getString("status")
            ));
        }

    } catch (SQLException e) {
    }
    return bookings;
}

public List<String> getStudioStatistics() {
    List<String> stats = new ArrayList<>();
    String sql = """
        SELECT s.name, COUNT(b.id) AS bookings_count
        FROM studios s
        LEFT JOIN bookings b ON s.id = b.studio_id AND b.status = 'CONFIRMED'
        GROUP BY s.id, s.name
        ORDER BY bookings_count DESC
    """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            String studioName = rs.getString("name");
            int count = rs.getInt("bookings_count");
            stats.add(studioName + " → " + count + " prenotazioni Attive");
        }

    } catch (SQLException e) {
    }

    return stats;
}

    public Booking findById(long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
