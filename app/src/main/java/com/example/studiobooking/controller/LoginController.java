package com.example.studiobooking.controller;

import com.example.studiobooking.dao.UserDAO;
import com.example.studiobooking.model.Utente;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton, registerButton;

    private UserDAO userDAO = new UserDAO();
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
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        Utente utente = userDAO.login(email, password);
        if (utente != null) {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.close();

            if (utente.isAdmin()) {
                openAdminHome(utente);
            } else {
                if (homeController != null) {
                    homeController.setUtenteLoggato(utente);
                }
                openUserHome(utente);
            }

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Home.fxml"));
            Parent root = loader.load();

            HomeController controller = loader.getController();
            controller.setUtenteLoggato(user);

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
