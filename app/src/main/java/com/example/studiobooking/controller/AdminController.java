package com.example.studiobooking.controller;

import com.example.studiobooking.dao.AdminDAO;
import com.example.studiobooking.model.Utente;
import com.example.studiobooking.model.Studio;
import com.example.studiobooking.model.Booking;
import com.example.studiobooking.model.Equipment;
import com.example.studiobooking.model.Studio;
import com.example.studiobooking.model.Utente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminController {

    

    @FXML private Label welcomeLabel;
    // Prenotazioni
    @FXML private ListView<Booking> bookingsListView;
    @FXML private Button addBookingButton, editBookingButton, deleteBookingButton;

    // Studi
    @FXML private ListView<Studio> studiosListView;
    @FXML private Button enableStudioButton, disableStudioButton;

    // Attrezzatura
    @FXML private ListView<Equipment> equipmentListView;
    @FXML private Button addEquipmentButton, toggleEquipmentButton, deleteEquipmentButton;

    // Creazione admin
    @FXML private TextField adminNameField, adminEmailField;
    @FXML private PasswordField adminPasswordField;
    @FXML private Button createAdminButton, enableStudioButton, disableStudioButton;
    @FXML private Button addBookingButton, editBookingButton, deleteBookingButton;

    private AdminDAO adminDAO = new AdminDAO();
    private ObservableList<Studio> studioObservableList = FXCollections.observableArrayList();
    private ObservableList<Booking> bookingObservableList = FXCollections.observableArrayList();
    private Utente loggedAdmin;

    public void initAdmin(Utente admin) {
        if (!admin.isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Non hai permessi di admin.");
            Stage stage = (Stage) createAdminButton.getScene().getWindow();
            stage.close();
            return;
        }

        // Mostra il welcome sotto il titolo
    if (welcomeLabel != null) {
        welcomeLabel.setText("Benvenuto, " + admin.getName() + "!");
    }

        loadStudios();
        loadBookings();
    }

    // ------------------- STUDI -------------------
    private void loadStudios() {
        List<Studio> studios = studioDAO.getAllStudios();
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

    @FXML private void enableStudio() {
        Studio selected = studiosListView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "Seleziona uno studio."); return; }
        if (selected.isActive()) { showAlert(Alert.AlertType.INFORMATION, "Studio già abilitato."); return; }
        boolean updated = studioDAO.updateStudioStatus(selected.getId(), true);
        showAlert(updated ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                updated ? "Studio abilitato!" : "Errore.");
        loadStudios();
    }

    @FXML private void disableStudio() {
        Studio selected = studiosListView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "Seleziona uno studio."); return; }
        if (!selected.isActive()) { showAlert(Alert.AlertType.INFORMATION, "Studio già disabilitato."); return; }
        boolean updated = studioDAO.updateStudioStatus(selected.getId(), false);
        showAlert(updated ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                updated ? "Studio disabilitato!" : "Errore.");
        loadStudios();
    }

    // ------------------- PRENOTAZIONI -------------------
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

    @FXML private void editBooking() {
        Booking selected = bookingsListView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "Seleziona una prenotazione."); return; }
        BookingData data = showBookingDialog(selected);
        if (data != null) {
            boolean success = bookingDAO.updateBooking(selected.getId(), data.studioId, data.start, data.end, data.status);
            showAlert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                      success ? "Prenotazione modificata!" : "Errore o conflitto.");
            loadBookings();
        }
    }

    @FXML private void deleteBooking() {
        Booking selected = bookingsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean updated = adminDAO.updateBookingStatus(selected.getId(), "Modificato");
            if (updated) loadBookings();
        }
    }
}
