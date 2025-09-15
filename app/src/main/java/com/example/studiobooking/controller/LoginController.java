package com.example.studiobooking.controller;

import com.example.studiobooking.dao.UserDAO;
import com.example.studiobooking.model.Utente;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;

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

        Utente utente = userDAO.login(email, password);
        if (utente != null) {
            // Finestra di benvenuto
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Benvenuto!");
            alert.setHeaderText(null);
            alert.setContentText("Ciao " + utente.getName() + ", bentornato su Studio Booking!");
            alert.showAndWait();

            // Carica schermata studi
            try {
                URL fxmlLocation = getClass().getResource("/view/StudiosView.fxml");

                FXMLLoader loader = new FXMLLoader(fxmlLocation);
                Parent root = loader.load();

                // Passa l'utente loggato al controller
                StudiosController controller = loader.getController();
                controller.setUtenteLoggato(utente);

                Stage stage = (Stage) loginButton.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Studio Booking - Studi Disponibili");

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
