package com.example.studiobooking.controller;

import com.example.studiobooking.dao.UtenteDAO;
import com.example.studiobooking.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthFXController {

    private UtenteDAO utenteDAO = new UtenteDAO();

    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;

    @FXML private TextField regName;
    @FXML private TextField regEmail;
    @FXML private PasswordField regPassword;

    @FXML
    public void handleLogin() {
        try {
            Utente utente = utenteDAO.getUtenteByEmail(loginEmail.getText());
            if (utente != null && utente.getPasswordHash().equals(loginPassword.getText())) {
                showAlert("Successo", "Login effettuato: " + utente.getName());
            } else {
                showAlert("Errore", "Email o password errati");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore", e.getMessage());
        }
    }

    @FXML
    public void handleRegister() {
        try {
            if (utenteDAO.getUtenteByEmail(regEmail.getText()) != null) {
                showAlert("Errore", "Email già registrata");
                return;
            }
            Utente utente = new Utente(0, regEmail.getText(), regPassword.getText(), regName.getText(), null);
            utenteDAO.createUtente(utente);
            showAlert("Successo", "Registrazione effettuata!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Errore", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
