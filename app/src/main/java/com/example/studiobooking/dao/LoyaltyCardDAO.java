package com.example.studiobooking.dao;

import com.example.studiobooking.model.LoyaltyCard;

import java.sql.*;

public class LoyaltyCardDAO {

    // Recupera la loyalty card di un utente
    public LoyaltyCard getLoyaltyCardByUserId(long userId) {
        String sql = "SELECT * FROM loyalty_cards WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new LoyaltyCard(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getInt("Total_booking"),   // qui usiamo Total_booking
                    rs.getInt("discount_level")
                );
            }

        } catch (SQLException e) {
            System.err.println("Errore durante il recupero della LoyaltyCard per userId=" + userId);
            e.printStackTrace();
        }
        return null;
    }

    // Crea una nuova loyalty card per un utente
    public boolean createLoyaltyCard(long userId) {
        String sql = "INSERT INTO loyalty_cards (user_id, total_booking, discount_level) VALUES (?, 0, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Errore durante la creazione della LoyaltyCard per userId=" + userId);
            e.printStackTrace();
        }
        return false;
    }

    // Aggiorna il livello di sconto in base al totale prenotazioni
    public boolean updateDiscountLevel(long userId, int totalBookings) {
        // Ogni 3 prenotazioni = 5% sconto, massimo 30%
        int newDiscount = Math.min((totalBookings / 3) * 5, 30);

        String sql = "UPDATE loyalty_cards SET total_booking = ?, discount_level = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, totalBookings);
            stmt.setInt(2, newDiscount);
            stmt.setLong(3, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Errore durante l'aggiornamento della LoyaltyCard per userId=" + userId);
            e.printStackTrace();
        }
        return false;
    }

    public boolean addBookings(long userId, int delta) {
    try (Connection conn = DatabaseConnection.getConnection()) {
        // Leggi il totale attuale
        PreparedStatement select = conn.prepareStatement(
            "SELECT Total_booking FROM loyalty_cards WHERE user_id = ?"
        );
        select.setLong(1, userId);
        ResultSet rs = select.executeQuery();
        if (!rs.next()) return false;

        int totalBookings = rs.getInt("Total_booking");
        int newTotal = Math.max(totalBookings + delta, 0); // non scende sotto 0

        // Aggiorna totale e livello sconto
        int newDiscount = Math.min((newTotal / 3) * 5, 30);

        PreparedStatement update = conn.prepareStatement(
            "UPDATE loyalty_cards SET Total_booking = ?, discount_level = ? WHERE user_id = ?"
        );
        update.setInt(1, newTotal);
        update.setInt(2, newDiscount);
        update.setLong(3, userId);

        return update.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
    public void refreshLoyaltyCard(long userId) {
    try (Connection conn = DatabaseConnection.getConnection()) {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT COUNT(*) AS total FROM bookings WHERE user_id = ? AND status != 'CANCELLED'"
        );
        stmt.setLong(1, userId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            int totalBookings = rs.getInt("total");

            // Aggiorna loyalty card
            updateDiscountLevel(userId, totalBookings);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


}
