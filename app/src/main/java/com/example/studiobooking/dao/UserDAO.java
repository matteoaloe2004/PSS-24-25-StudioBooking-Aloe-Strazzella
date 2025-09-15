package com.example.studiobooking.dao;

import com.example.studiobooking.model.Utente;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // 🔑 LOGIN con password hashata (BCrypt)
    public Utente login(String email, String plainPassword) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                // Controllo password con BCrypt
                if (BCrypt.checkpw(plainPassword, storedHash)) {
                    return new Utente(
                            rs.getLong("id"),
                            rs.getString("email"),
                            storedHash,
                            rs.getString("name"),
                            rs.getTimestamp("created_at")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // login fallito
    }

    // 📝 REGISTRAZIONE con hash password
    public boolean register(Utente utente) {
        // Controllo se email già esiste
        if (emailExists(utente.getEmail())) {
            System.out.println("Email già esistente: " + utente.getEmail());
            return false;
        }

        String sql = "INSERT INTO users (email, password_hash, name) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utente.getEmail());
            stmt.setString(2, utente.getPasswordHash()); // già hashata dal RegisterController
            stmt.setString(3, utente.getName());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 📧 Controllo se email esiste già
    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
