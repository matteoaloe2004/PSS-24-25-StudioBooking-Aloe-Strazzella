package com.example.studiobooking.controller;

import com.example.studiobooking.dao.EquipmentDAO;
import com.example.studiobooking.model.Booking;
import com.example.studiobooking.model.Equipment;
import com.example.studiobooking.model.Studio;
import com.example.studiobooking.model.Utente;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class BookingController {

    @FXML private Label studioDescriptionLabel;
    @FXML private Label studioLabel;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeSlotComboBox;

    @FXML private ComboBox<Equipment> microphoneComboBox;
    @FXML private ComboBox<Equipment> audioInterfaceComboBox;
    @FXML private ComboBox<Equipment> monitorComboBox;

    @FXML private Button confirmButton;
    @FXML private Button backButton;

    private Utente utenteLoggato;
    private Studio studioSelezionato;
    private HomeController homeController;

    private EquipmentDAO equipmentDAO = new EquipmentDAO();
    private BookingDAO bookingDAO = new BookingDAO();

    private ObservableList<Equipment> equipmentObservableList = FXCollections.observableArrayList();

    public void initBooking(Utente utente, Studio studio) {
        this.utenteLoggato = utente;
        this.studioSelezionato = studio;

        studioLabel.setText("Prenotazione: " + studio.getName());

        // Limita datePicker a 1 mese
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate now = LocalDate.now();
                setDisable(empty || date.isBefore(now) || date.isAfter(now.plusMonths(1)));
            }
        });
        datePicker.setValue(LocalDate.now());

        // Aggiorna fasce orarie quando cambia la data
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> updateTimeSlots(existingBookings, newDate));

        // Fasce orarie
        timeSlotComboBox.getItems().addAll(
                "09:00 - 11:00",
                "11:00 - 13:00",
                "14:00 - 16:00",
                "16:00 - 18:00"
        );
        timeSlotComboBox.getSelectionModel().selectFirst();

        // Carica attrezzature
        loadEquipment();
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    private void loadEquipment() {
        List<Equipment> equipmentList = equipmentDAO.getEquipmentByStudio(studioSelezionato.getId());
        equipmentObservableList.setAll(equipmentList);
        equipmentListView.setItems(equipmentObservableList);
        equipmentListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        equipmentListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            public void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    @FXML
    public void initialize() {
        confirmButton.setOnAction(e -> confirmBooking());
        backButton.setOnAction(e -> goBackToHome());
    }

    private void goBackToHome() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private boolean checkRequiredEquipment(List<Equipment> selectedEquipment) {
        boolean hasMicrophone = false, hasAudioInterface = false, hasMonitor = false;
        for (Equipment e : selectedEquipment) {
            switch (e.getType()) {
                case "MIC": hasMicrophone = true; break;
                case "AUDIO": hasAudioInterface = true; break;
                case "MONITOR": hasMonitor = true; break;
            }
        }
        if (!hasMicrophone) { showAlert(Alert.AlertType.WARNING, "Devi selezionare almeno un microfono."); return false; }
        if (!hasAudioInterface) { showAlert(Alert.AlertType.WARNING, "Devi selezionare almeno una scheda audio."); return false; }
        if (!hasMonitor) { showAlert(Alert.AlertType.WARNING, "Devi selezionare almeno un monitor."); return false; }
        return true;
    }

    private void confirmBooking() {
        LocalDate date = datePicker.getValue();
        String timeSlot = timeSlotComboBox.getSelectionModel().getSelectedItem();
        List<Equipment> selectedEquipment = equipmentListView.getSelectionModel().getSelectedItems();

        if (date == null || timeSlot == null) {
            showAlert(Alert.AlertType.WARNING, "Seleziona giorno e fascia oraria.");
            return;
        }

        if (selectedEquipment.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Seleziona almeno un pezzo di attrezzatura.");
            return;
        }

        String[] times = timeSlot.split(" - ");
        LocalTime startTime = LocalTime.parse(times[0].trim());
        LocalTime endTime = LocalTime.parse(times[1].trim());

        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        if (!bookingDAO.isAvailable(studioSelezionato.getId(), startDateTime, endDateTime)) {
            showAlert(Alert.AlertType.ERROR, "Lo studio non è disponibile in questa fascia oraria.");
            return;
        }

        if (!bookingDAO.isEquipmentAvailable(selectedMicrophone, startDateTime, endDateTime) ||
        !bookingDAO.isEquipmentAvailable(selectedAudio, startDateTime, endDateTime) ||
        !bookingDAO.isEquipmentAvailable(selectedMonitor, startDateTime, endDateTime)) {
    
        showAlert(Alert.AlertType.ERROR, "Alcuna delle attrezzature selezionate non è disponibile in questa fascia oraria.");
        return;
    }

        boolean success = bookingDAO.createBooking(
                utenteLoggato.getId(),
                studioSelezionato.getId(),
                startDateTime,
                endDateTime,
                List.of(selectedMicrophone, selectedAudio, selectedMonitor)
        );

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Prenotazione confermata per " +
                    studioSelezionato.getName() + " il " + date + " " + timeSlot);

            if (homeController != null) {
                homeController.loadUserBookings();
                homeController.loadLoyaltyCard();
            }
            goBackToHome();
        } else {
            showAlert(Alert.AlertType.ERROR, "Errore durante la prenotazione.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }
}
