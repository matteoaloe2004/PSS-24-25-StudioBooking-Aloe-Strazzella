package com.example.studiobooking.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.mindrot.jbcrypt.BCrypt;

import com.example.studiobooking.model.Utente;

public class UserDAO {

    // LOGIN
    public Utente login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.trim().toLowerCase());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("Password_Hash");

                // Confronto password hashata
                if (BCrypt.checkpw(password, hashedPassword)) {
                    return new Utente(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            hashedPassword,
                            rs.getTimestamp("created_at"),
                            rs.getBoolean("is_admin")
                    );
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

    // REGISTRA UN UTENTE NORMALE
    public boolean register(Utente utente) {
        if (emailExists(utente.getEmail())) {
            return false; // email già esistente
        }

        String sql = "INSERT INTO users (name, email, Password_Hash, created_at, is_admin) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // HASH della password qui
            String hashedPassword = BCrypt.hashpw(utente.getPassword(), BCrypt.gensalt());

            stmt.setString(1, utente.getName());
            stmt.setString(2, utente.getEmail().trim().toLowerCase());
            stmt.setString(3, hashedPassword);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.setBoolean(5, utente.isAdmin());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // CREA UN ADMIN
    public boolean createAdmin(String name, String email, String password) {
        String sql = "INSERT INTO users (name, email, Password_Hash, created_at, is_admin) VALUES (?, ?, ?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            stmt.setString(1, name);
            stmt.setString(2, email.trim().toLowerCase());
            stmt.setString(3, hashedPassword);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}