package com.example.studiobooking.dao;

import com.example.studiobooking.model.Utente;
import com.example.studiobooking.model.LoyaltyCard;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class UserDAO {

    // LOGIN UTENTE
    public Utente login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim().toLowerCase());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password_hash");

                if (BCrypt.checkpw(password, hashedPassword)) {
                    Utente user = new Utente(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            hashedPassword,
                            rs.getTimestamp("created_at"),
                            rs.getBoolean("is_admin")
                    );

                    // Recupera la loyalty card
                    LoyaltyCardDAO cardDAO = new LoyaltyCardDAO();
                    LoyaltyCard card = cardDAO.getLoyaltyCardByUserId(user.getId());
                    user.setLoyaltyCard(card);

                    return user;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // CONTROLLA SE EMAIL ESISTE
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim().toLowerCase());
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // REGISTRA UTENTE NORMALE
    public boolean register(Utente utente) {
        if (emailExists(utente.getEmail())) return false;

        String sql = "INSERT INTO users (name, email, password_hash, created_at, is_admin) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String hashedPassword = BCrypt.hashpw(utente.getPassword(), BCrypt.gensalt());

            stmt.setString(1, utente.getName());
            stmt.setString(2, utente.getEmail().trim().toLowerCase());
            stmt.setString(3, hashedPassword);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.setBoolean(5, false); // utenti normali non sono admin

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    long newUserId = rs.getLong(1);
                    // Crea loyalty card automaticamente
                    LoyaltyCardDAO loyaltyCardDAO = new LoyaltyCardDAO();
                    loyaltyCardDAO.createLoyaltyCard(newUserId);
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // CREA ADMIN
    public boolean createAdmin(String name, String email, String password) {
        if (emailExists(email)) return false;

        String sql = "INSERT INTO users (name, email, password_hash, created_at, is_admin) VALUES (?, ?, ?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            stmt.setString(1, name);
            stmt.setString(2, email.trim().toLowerCase());
            stmt.setString(3, hashedPassword);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    long newUserId = rs.getLong(1);
                    // Creazione loyalty card automatica
                    LoyaltyCardDAO loyaltyCardDAO = new LoyaltyCardDAO();
                    loyaltyCardDAO.createLoyaltyCard(newUserId);
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // RECUPERA UTENTE PER EMAIL
    public Utente getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim().toLowerCase());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Utente user = new Utente(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getTimestamp("created_at"),
                        rs.getBoolean("is_admin")
                );

                // Recupera la loyalty card
                LoyaltyCardDAO cardDAO = new LoyaltyCardDAO();
                LoyaltyCard card = cardDAO.getLoyaltyCardByUserId(user.getId());
                user.setLoyaltyCard(card);

                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
