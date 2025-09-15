package com.example.studiobooking.dao;

import com.example.studiobooking.model.Equipment;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BookingDAO {

    // Controlla se lo studio è disponibile per la data e fascia selezionata
    public boolean isAvailable(long studioId, LocalDate date, String timeSlot) {
        String sql = "SELECT * FROM bookings WHERE studio_id = ? AND " +
                     "((start_time < ? AND end_time > ?) OR " +
                     "(start_time < ? AND end_time > ?) OR " +
                     "(start_time >= ? AND end_time <= ?))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Split fascia oraria
            String[] times = timeSlot.split(" - ");
            String[] startParts = times[0].split(":");
            String[] endParts = times[1].split(":");

            LocalDateTime startDateTime = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(),
                                                          Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]));
            LocalDateTime endDateTime = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(),
                                                        Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]));

            // Imposta parametri
            stmt.setLong(1, studioId);
            stmt.setTimestamp(2, Timestamp.valueOf(endDateTime));
            stmt.setTimestamp(3, Timestamp.valueOf(startDateTime));
            stmt.setTimestamp(4, Timestamp.valueOf(endDateTime));
            stmt.setTimestamp(5, Timestamp.valueOf(startDateTime));
            stmt.setTimestamp(6, Timestamp.valueOf(startDateTime));
            stmt.setTimestamp(7, Timestamp.valueOf(endDateTime));

            ResultSet rs = stmt.executeQuery();
            return !rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Crea prenotazione
    public boolean createBooking(long userId, long studioId, LocalDate date, String timeSlot, List<Equipment> equipmentList) {
        String sql = "INSERT INTO bookings (user_id, studio_id, start_time, end_time, status) VALUES (?, ?, ?, ?, 'CONFIRMED')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Split fascia oraria
            String[] times = timeSlot.split(" - ");
            String[] startParts = times[0].split(":");
            String[] endParts = times[1].split(":");

            LocalDateTime startDateTime = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(),
                                                          Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]));
            LocalDateTime endDateTime = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(),
                                                        Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]));

            // Imposta parametri
            stmt.setLong(1, userId);
            stmt.setLong(2, studioId);
            stmt.setTimestamp(3, Timestamp.valueOf(startDateTime));
            stmt.setTimestamp(4, Timestamp.valueOf(endDateTime));

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
