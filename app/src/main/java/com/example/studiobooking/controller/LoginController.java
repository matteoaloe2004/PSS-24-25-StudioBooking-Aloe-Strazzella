package com.example.studiobooking.controller;

import com.example.studiobooking.dao.UserDAO;
import com.example.studiobooking.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton, registerButton;

    private UserDAO userDAO = new UserDAO();

    // Riferimento alla home principale
    private HomeController homeController;

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

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
            // Aggiorna la home principale
            if (homeController != null) {
                homeController.setUtenteLoggato(utente);
            }

            // Chiudi il popup di login
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();

            // Messaggio di benvenuto
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Benvenuto!");
            alert.setHeaderText(null);
            alert.setContentText("Ciao " + utente.getName() + ", bentornato su Studio Booking!");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Email o password errati!");
            alert.showAndWait();
        }
    }

    private void openRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load(), 400, 300));
            stage.setTitle("Registrazione");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
