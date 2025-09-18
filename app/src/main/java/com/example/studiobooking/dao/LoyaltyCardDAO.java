package com.example.studiobooking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.studiobooking.model.LoyaltyCard;

public class LoyaltyCardDAO {

    // Recupera la loyalty card di un utente
    public LoyaltyCard getLoyaltyCardByUserId(long userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM loyalty_cards WHERE user_id = ?"
            );
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new LoyaltyCard(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getInt("Total_booking"),
                        rs.getInt("discount_level")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Crea una nuova loyalty card per un utente
    public boolean createLoyaltyCard(long userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO loyalty_cards (user_id, Total_booking, discount_level) VALUES (?, 0, 0)"
            );
            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Aggiorna il numero totale di prenotazioni e il livello di sconto
    public boolean updateDiscountLevel(long userId, int totalBookings) {
        int newDiscount = Math.min((totalBookings / 3) * 5, 30); // max 30%
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE loyalty_cards SET Total_booking = ?, discount_level = ? WHERE user_id = ?"
            );
            stmt.setInt(1, totalBookings);
            stmt.setInt(2, newDiscount);
            stmt.setLong(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Aggiorna i dati della loyalty card in base alle prenotazioni NON cancellate
    public void refreshLoyaltyCard(long userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) AS total FROM bookings WHERE user_id = ? AND status != 'CANCELLED'"
            );
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int totalBookings = rs.getInt("total");
                updateDiscountLevel(userId, totalBookings);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
