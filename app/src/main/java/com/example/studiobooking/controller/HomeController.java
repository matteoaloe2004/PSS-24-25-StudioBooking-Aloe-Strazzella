package com.example.studiobooking.controller;

import com.example.studiobooking.dao.StudioDAO;
import com.example.studiobooking.model.Studio;
import com.example.studiobooking.model.Utente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button loginButton, registerButton, bookButton, logoutButton;

    @FXML
    private ListView<Studio> studiosListView;

    private StudioDAO studioDAO = new StudioDAO();
    private ObservableList<Studio> studioObservableList = FXCollections.observableArrayList();
    private Utente utenteLoggato;

    @FXML
    public void initialize() {
        loadStudios();

        loginButton.setOnAction(e -> openLogin());
        registerButton.setOnAction(e -> openRegister());
        bookButton.setOnAction(e -> openBooking());
        logoutButton.setOnAction(e -> logout());

        logoutButton.setVisible(false); // logout nascosto all'avvio
    }

    public void setUtenteLoggato(Utente utente) {
        this.utenteLoggato = utente;
        if (utente != null) {
            welcomeLabel.setText("Ciao " + utente.getName() + ", bentornato!");
            loginButton.setVisible(false);
            registerButton.setVisible(false);
            logoutButton.setVisible(true);
        }
    }

    private void logout() {
        utenteLoggato = null;
        welcomeLabel.setText("Benvenuto su Studio Booking!");
        loginButton.setVisible(true);
        registerButton.setVisible(true);
        logoutButton.setVisible(false);
    }

    private void loadStudios() {
        List<Studio> studios = studioDAO.getActiveStudios();
        studioObservableList.setAll(studios);
        studiosListView.setItems(studioObservableList);
    }

    private void openLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();
            loginController.setHomeController(this); // Passa riferimento a questa home

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
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

    private void openBooking() {
        Studio selected = studiosListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Seleziona uno studio prima di prenotare.");
            alert.showAndWait();
            return;
        }

        if (utenteLoggato == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Devi essere loggato per prenotare uno studio.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookingView.fxml"));
            Parent root = loader.load();

            BookingController controller = loader.getController();
            controller.initBooking(utenteLoggato, selected);

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 600)); // finestra piu grande
            stage.setTitle("Prenotazione Studio: " + selected.getName());
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
