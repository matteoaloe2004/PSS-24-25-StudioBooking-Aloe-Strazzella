package com.example.studiobooking.controller;

import com.example.studiobooking.dao.UserDAO;
import com.example.studiobooking.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField nameField, emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button registerButton;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        registerButton.setOnAction(e -> registerUser());
    }

    private void registerUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Tutti i campi sono obbligatori.");
            return;
        }

        // Controllo email già esistente
        if (userDAO.emailExists(email)) {
            showAlert("Email già registrata!");
            return;
        }

        // Per ora password in chiaro, poi hash con SHA-256
        Utente utente = new Utente(0, name, email, password, null);

        boolean success = userDAO.register(utente);
        if (success) {
            showAlert("Registrazione completata! Ora puoi fare login.");
            // Chiudi finestra registrazione
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.close();
        } else {
            showAlert("Errore durante la registrazione.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }
}
