package com.example.studiobooking.controller;

import com.example.studiobooking.dao.AdminDAO;
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
    @FXML private Button addBookingButton, editBookingButton, deleteBookingButton;

    private AdminDAO adminDAO = new AdminDAO();
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
        List<Studio> studios = adminDAO.getAllStudios();
        studioObservableList.setAll(studios);
        studiosListView.setItems(studioObservableList);

        studiosListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Studio studio, boolean empty) {
                super.updateItem(studio, empty);
                if (empty || studio == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(studio.getName() + " (" + (studio.isActive() ? "Attivo" : "Disabilitato") + ")");
                    setStyle(studio.isActive() ? "-fx-text-fill:black;" : "-fx-text-fill:gray; -fx-opacity:0.6;");
                }
            }
        });
    }

    private void loadBookings() {
        List<Booking> bookings = adminDAO.getAllBookings();
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

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        boolean success = adminDAO.createAdmin(name, email, hashedPassword);

        Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                success ? "Admin creato con successo!" : "Errore durante la creazione dell'admin.");
        alert.showAndWait();
    }

    @FXML
    private void enableStudio() {
        Studio selected = studiosListView.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isActive()) {
            adminDAO.updateStudioStatus(selected.getId(), true);
            loadStudios();
        }
    }

    @FXML
    private void disableStudio() {
        Studio selected = studiosListView.getSelectionModel().getSelectedItem();
        if (selected != null && selected.isActive()) {
            adminDAO.updateStudioStatus(selected.getId(), false);
            loadStudios();
        }
    }

    @FXML
    private void deleteBooking() {
        Booking selected = bookingsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            adminDAO.deleteBooking(selected.getId());
            loadBookings();
        }
    }

    @FXML
    private void editBooking() {
        Booking selected = bookingsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean updated = adminDAO.updateBookingStatus(selected.getId(), "Modificato");
            if (updated) loadBookings();
        }
    }
}
