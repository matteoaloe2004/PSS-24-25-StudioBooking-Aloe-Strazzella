package com.example.studiobooking.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.example.studiobooking.dao.BookingDAO;
import com.example.studiobooking.dao.EquipmentDAO;
import com.example.studiobooking.dao.StudioDAO;
import com.example.studiobooking.dao.UserDAO;
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
    @FXML private Button createAdminButton;

    private final UserDAO userDAO = new UserDAO();
    private final StudioDAO studioDAO = new StudioDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();

    private final ObservableList<Studio> studioObservableList = FXCollections.observableArrayList();
    private final ObservableList<Booking> bookingObservableList = FXCollections.observableArrayList();
    private final ObservableList<Equipment> equipmentObservableList = FXCollections.observableArrayList();
    
    // ------------------- INIT -------------------
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
        loadEquipment();
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
        List<Booking> bookings = bookingDAO.getAllBookings();
        bookingObservableList.setAll(bookings);
        bookingsListView.setItems(bookingObservableList);
    }

    @FXML private void addBooking() {
        BookingData data = showBookingDialog(null);
        if (data != null) {
            boolean success = bookingDAO.createBooking(data.userId, data.studioId, data.start, data.end, new ArrayList<>());
            showAlert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                      success ? "Prenotazione aggiunta!" : "Errore o conflitto.");
            loadBookings();
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
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "Seleziona una prenotazione."); return; }
        boolean deleted = bookingDAO.deleteBooking(selected.getId());
        showAlert(deleted ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                  deleted ? "Prenotazione rimossa!" : "Errore.");
        loadBookings();
    }

    private BookingData showBookingDialog(Booking booking) {
    Dialog<BookingData> dialog = new Dialog<>();
    dialog.setTitle(booking == null ? "Aggiungi Prenotazione" : "Modifica Prenotazione");
    ButtonType okButton = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

    GridPane grid = new GridPane(); 
    grid.setHgap(10); grid.setVgap(10); 
    grid.setPadding(new Insets(20,150,10,10));

    ComboBox<Studio> studioCombo = new ComboBox<>(studioObservableList);
    DatePicker startDate = new DatePicker();
    TextField startTime = new TextField(); // HH:mm
    TextField endTime = new TextField();   // HH:mm

    if (booking != null) {
        studioCombo.getSelectionModel().select(
            studioObservableList.stream().filter(s -> s.getId() == booking.getStudioId()).findFirst().orElse(null));
        startDate.setValue(booking.getStartTime().toLocalDate());
        startTime.setText(booking.getStartTime().toLocalTime().toString());
        endTime.setText(booking.getEndTime().toLocalTime().toString());
    }

    grid.add(new Label("Studio:"),0,0); grid.add(studioCombo,1,0);
    grid.add(new Label("Data Inizio:"),0,1); grid.add(startDate,1,1);
    grid.add(new Label("Ora Inizio (HH:mm):"),0,2); grid.add(startTime,1,2);
    grid.add(new Label("Ora Fine (HH:mm):"),0,3); grid.add(endTime,1,3);

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(btn -> {
        if (btn == okButton) {
            Studio selectedStudio = studioCombo.getSelectionModel().getSelectedItem();
            if (selectedStudio == null) return null;
            LocalDateTime start = LocalDateTime.parse(startDate.getValue() + "T" + startTime.getText());
            LocalDateTime end = LocalDateTime.parse(startDate.getValue() + "T" + endTime.getText());
            // Lo stato è sempre CONFIRMED
            return new BookingData(1, selectedStudio.getId(), start, end, "CONFIRMED");
        }
        return null;
    });

    return dialog.showAndWait().orElse(null);
}

    private static class BookingData {
        long userId, studioId; LocalDateTime start, end; String status;
        BookingData(long u, long s, LocalDateTime start, LocalDateTime end, String st) {
            this.userId=u; this.studioId=s; this.start=start; this.end=end; this.status=st;
        }
    }

    // ------------------- ATTREZZATURA -------------------
    private void loadEquipment() {
        List<Equipment> list = equipmentDAO.getAllEquipment();
        equipmentObservableList.setAll(list);
        equipmentListView.setItems(equipmentObservableList);

        equipmentListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Equipment eq, boolean empty) {
                super.updateItem(eq, empty);
                if (empty || eq==null) setText(null);
                else setText(eq.getName() + " (" + (eq.isAvailable() ? "Disponibile":"Non disponibile") + ")");
            }
        });
    }

    @FXML private void addEquipment() {
        Dialog<EquipmentData> dialog = new Dialog<>();
        dialog.setTitle("Aggiungi Attrezzatura");
        ButtonType okButton = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20,150,10,10));
        TextField nameField = new TextField(); nameField.setPromptText("Nome");
        TextField descField = new TextField(); descField.setPromptText("Descrizione");

        VBox studioBox = new VBox(5); List<CheckBox> studioCheckBoxes = new ArrayList<>();
        for (Studio s : studioObservableList) {
            CheckBox cb = new CheckBox(s.getName()); cb.setUserData(s.getId());
            studioCheckBoxes.add(cb); studioBox.getChildren().add(cb);
        }

        grid.add(new Label("Nome:"),0,0); grid.add(nameField,1,0);
        grid.add(new Label("Descrizione:"),0,1); grid.add(descField,1,1);
        grid.add(new Label("Assegna a studi:"),0,2); grid.add(studioBox,1,2);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn==okButton) {
                String name=nameField.getText().trim();
                String desc=descField.getText().trim();
                List<Long> selectedIds=studioCheckBoxes.stream().filter(CheckBox::isSelected)
                        .map(cb->(Long)cb.getUserData()).toList();
                return new EquipmentData(name, desc, selectedIds);
            }
            return null;
        });

        EquipmentData data = dialog.showAndWait().orElse(null);
        if (data!=null && !data.studioIds.isEmpty()) {
            long eqId = equipmentDAO.addEquipment(data.name, data.description);
            equipmentDAO.assignEquipmentToStudios(eqId, data.studioIds);
            loadEquipment();
        }
    }

    @FXML private void toggleEquipment() {
        Equipment selected = equipmentListView.getSelectionModel().getSelectedItem();
        if (selected==null) { showAlert(Alert.AlertType.WARNING,"Seleziona un equip."); return; }
        boolean updated = equipmentDAO.toggleEquipment(selected.getId());
        showAlert(updated ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                updated ? "Aggiornato!" : "Errore");
        loadEquipment();
    }

    @FXML private void deleteEquipment() {
        Equipment selected = equipmentListView.getSelectionModel().getSelectedItem();
        if (selected==null) { showAlert(Alert.AlertType.WARNING,"Seleziona un equip."); return; }
        boolean deleted = equipmentDAO.deleteEquipment(selected.getId());
        showAlert(deleted ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                deleted ? "Rimosso!" : "Errore");
        loadEquipment();
    }

    private static class EquipmentData {
        String name, description; List<Long> studioIds;
        EquipmentData(String n, String d, List<Long> ids){name=n; description=d; studioIds=ids;}
    }

    // ------------------- CREAZIONE ADMIN -------------------
    @FXML private void createAdmin() {
        String name = adminNameField.getText().trim();
        String email = adminEmailField.getText().trim();
        String password = adminPasswordField.getText().trim();
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Compila tutti i campi.");
            return;
        }

        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        long id = 0L;
        Timestamp now = new Timestamp(System.currentTimeMillis());

        Utente nuovoAdmin = new Utente(id, name, email, hashed, now, true);
        boolean success = userDAO.register(nuovoAdmin);

        showAlert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                  success ? "Admin creato!" : "Errore nella creazione.");
    }

    // ------------------- ALERT -------------------
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
}
