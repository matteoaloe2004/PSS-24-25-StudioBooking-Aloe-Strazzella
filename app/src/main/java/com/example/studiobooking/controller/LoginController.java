package com.example.studiobooking.controller;

import com.example.studiobooking.dao.UserDAO;
import com.example.studiobooking.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton, registerButton;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        loginButton.setOnAction(e -> login());
        registerButton.setOnAction(e -> openRegister());
    }

    private void login() {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Per ora usiamo il plain text, poi puoi hashare con SHA-256
        Utente utente = userDAO.login(email, password);
        if (utente != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Login effettuato!");
            alert.showAndWait();

            // Apri la schermata degli studi
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/StudiosView.fxml"));
                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Email o password errati!");
            alert.showAndWait();
        }
    }

    private void openRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Registrazione");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
