package com.example.studiobooking.controller;

import com.example.studiobooking.dao.UserDAO;
import com.example.studiobooking.dao.StudioDAO;
import com.example.studiobooking.dao.BookingDAO;
import com.example.studiobooking.model.Utente;
import com.example.studiobooking.model.Studio;
import com.example.studiobooking.model.Booking;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class AdminController {

    @FXML private ListView<Studio> studiosListView;
    @FXML private ListView<Booking> bookingsListView;
    @FXML private TextField adminNameField, adminEmailField;
    @FXML private PasswordField adminPasswordField;
    @FXML private Button createAdminButton, enableStudioButton, disableStudioButton;

    private UserDAO userDAO = new UserDAO();
    private StudioDAO studioDAO = new StudioDAO();
    private BookingDAO bookingDAO = new BookingDAO();

    private ObservableList<Studio> studioObservableList = FXCollections.observableArrayList();
    private ObservableList<Booking> bookingObservableList = FXCollections.observableArrayList();

    private Utente loggedAdmin;

    public void initAdmin(Utente admin) {
        this.loggedAdmin = admin;
        if (!admin.isAdmin()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Non hai permessi di admin.");
            alert.showAndWait();
            Stage stage = (Stage) createAdminButton.getScene().getWindow();
            stage.close();
            return;
        }
        loadStudios();
        loadBookings();
    }

    private void loadStudios() {
        List<Studio> studios = studioDAO.getAllStudios();
        studioObservableList.setAll(studios);
        studiosListView.setItems(studioObservableList);
    }

    private void loadBookings() {
        List<Booking> bookings = bookingDAO.getAllBookings();
        bookingObservableList.setAll(bookings);
        bookingsListView.setItems(bookingObservableList);
    }

    @FXML
    private void createAdmin() {
        String name = adminNameField.getText().trim();
        String email = adminEmailField.getText().trim();
        String password = adminPasswordField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Compila tutti i campi.");
            alert.showAndWait();
            return;
        }

        // Hash password con BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        boolean success = userDAO.createAdmin(name, email, hashedPassword);
        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Admin creato con successo!");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Errore durante la creazione dell'admin.");
            alert.showAndWait();
        }
    }

    @FXML
    private void enableStudio() {
        Studio selected = studiosListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setActive(true);
            studioDAO.updateStudioStatus(selected.getId(), true);
            loadStudios();
        }
    }

    @FXML
    private void disableStudio() {
        Studio selected = studiosListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setActive(false);
            studioDAO.updateStudioStatus(selected.getId(), false);
            loadStudios();
        }
    }
}
