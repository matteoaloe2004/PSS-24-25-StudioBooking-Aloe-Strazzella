package com.example.studiobooking.dao;

import com.example.studiobooking.model.LoyaltyCard;
import java.sql.*;

public class LoyaltyCardDAO {

    // Recupera la loyalty card di un utente
    public LoyaltyCard getCardByUser(long userId) {
        LoyaltyCard card = null;
        String sql = "SELECT user_id, total_bookings, discount_level FROM loyalty_cards WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                card = new LoyaltyCard(
                        rs.getLong("user_id"),
                        rs.getInt("total_bookings"),
                        rs.getInt("discount_level")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return card;
    }

    // Aggiorna il numero di prenotazioni e il livello di sconto
    public boolean updateCard(LoyaltyCard card) {
        String sql = "UPDATE loyalty_cards SET total_bookings = ?, discount_level = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, card.getTotalBookings());
            stmt.setInt(2, card.getDiscountLevel());
            stmt.setLong(3, card.getUserId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Incrementa una prenotazione per l'utente e aggiorna lo sconto
    public boolean addBookingForUser(long userId) {
        LoyaltyCard card = getCardByUser(userId);
        if (card == null) return false;

        card.addBooking(); // incrementa totalBookings e aggiorna discountLevel

        return updateCard(card);
    }
}
