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

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton, registerButton;

    private UserDAO userDAO = new UserDAO();

    // Campo per collegare la Home esistente
    private HomeController homeController;

    // Metodo pubblico per impostare la HomeController
    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    @FXML
    public void initialize() {
        loginButton.setOnAction(e -> login());
        registerButton.setOnAction(e -> openRegister());
    }

    private void login() {
        String email = emailField.getText().trim().toLowerCase();
        String password = passwordField.getText().trim();

        Utente utente = userDAO.login(email, password);
        if (utente != null) {
            if (utente.isAdmin()) {
                openAdminHome(utente);
            } else {
                // Aggiorna la Home esistente se presente
                if (homeController != null) {
                    homeController.setUtenteLoggato(utente);
                    homeController.loadUserBookings();
                } else {
                    openUserHome(utente);
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Benvenuto!");
            alert.setHeaderText(null);
            alert.setContentText("Ciao " + utente.getName() + ", bentornato su Studio Booking!");
            alert.showAndWait();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Email o password errati!");
            alert.showAndWait();
        }
    }

    private void openAdminHome(Utente admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminView.fxml"));
            Parent root = loader.load();

            AdminController controller = loader.getController();
            controller.initAdmin(admin);

            Stage stage = new Stage();
            stage.setTitle("Pannello Admin");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openUserHome(Utente user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeView.fxml"));
            Parent root = loader.load();

            HomeController controller = loader.getController();
            controller.setUtenteLoggato(user);
            controller.loadUserBookings();

            Stage stage = new Stage();
            stage.setTitle("Home Utente");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load(), 400, 300));
            stage.setTitle("Registrazione");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
