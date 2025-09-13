package com.example.studiobooking.controller;

import com.example.studiobooking.dao.UtenteDAO;
import com.example.studiobooking.model.Utente;
import javafx.scene.control.Alert;

import java.sql.SQLException;

public class AuthController {

    private UtenteDAO utenteDAO = new UtenteDAO();

    // Registrazione
    public boolean register(String name, String email, String passwordHash) {
        try {
            if (utenteDAO.getUtenteByEmail(email) != null) {
                showAlert("Errore", "Email già registrata");
                return false;
            }
            Utente utente = new Utente(0, email, passwordHash, name, null);
            utenteDAO.createUtente(utente);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Errore DB", e.getMessage());
            return false;
        }
    }

    // Login
    public Utente login(String email, String passwordHash) {
        try {
            Utente utente = utenteDAO.getUtenteByEmail(email);
            if (utente != null && utente.getPasswordHash().equals(passwordHash)) {
                return utente;
            } else {
                showAlert("Errore", "Email o password errati");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Errore DB", e.getMessage());
            return null;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
