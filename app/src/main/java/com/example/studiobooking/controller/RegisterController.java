package com.example.studiobooking.controller;

import com.example.studiobooking.dao.UserDAO;
import com.example.studiobooking.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Timestamp;

public class RegisterController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField nameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        registerButton.setOnAction(e -> registerUser());
    }

    private void registerUser() {
        String email = emailField.getText().trim().toLowerCase(); // normalizza email
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String name = nameField.getText().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Tutti i campi sono obbligatori.");
            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "La password deve avere almeno 6 caratteri.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Le password non coincidono.");
            return;
        }

        if (userDAO.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Email già registrata!");
            return;
        }

        // Creazione utente con PASSWORD IN CHIARO, UserDAO farà l'hash
        Utente utente = new Utente(
                0,                      // id = 0 per nuovo utente
                name,                   // name
                email,                  // email
                password,               // password in chiaro
                new Timestamp(System.currentTimeMillis()),
                false                   // isAdmin = false
        );

        boolean success = userDAO.register(utente);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Registrazione completata! Ora puoi fare login.");
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.close();
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore durante la registrazione.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
}
