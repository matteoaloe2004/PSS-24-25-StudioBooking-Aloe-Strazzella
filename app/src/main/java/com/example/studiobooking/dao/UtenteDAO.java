package com.example.studiobooking.dao;

import com.example.studiobooking.model.Utente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteDAO {

    // Creazione utente
    public void createUtente(Utente utente) throws SQLException {
        String sql = "INSERT INTO users (email, password_hash, name) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, utente.getEmail());
            stmt.setString(2, utente.getPasswordHash());
            stmt.setString(3, utente.getName());
            stmt.executeUpdate();
        }
    }

    // Recupero utente per email
    public Utente getUtenteByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Utente(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("name"),
                        rs.getTimestamp("created_at")
                );
            }
        }
        return null;
    }
}
