package com.example.studiobooking.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.studiobooking.dao.BookingDAO;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;
import javafx.util.Callback;

public class BookingController {

    @FXML private Label studioDescriptionLabel;
    @FXML private Label studioLabel;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeSlotComboBox;
    @FXML private ListView<Equipment> equipmentListView;
    @FXML private Button confirmButton;
    @FXML private Button backButton;

    private Utente utenteLoggato;
    private Studio studioSelezionato;
    private HomeController homeController;

    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    private final ObservableList<Equipment> equipmentObservableList = FXCollections.observableArrayList();
    private final ObservableList<Equipment> selectedEquipment = FXCollections.observableArrayList();
    private final Map<Equipment, BooleanProperty> equipmentSelectionMap = new HashMap<>();

    public void initBooking(Utente utente, Studio studio) {
        this.utenteLoggato = utente;
        this.studioSelezionato = studio;

        studioLabel.setText("Prenotazione: " + studio.getName());
        studioDescriptionLabel.setText(studio.getDescription());

        List<Booking> existingBookings = bookingDAO.getBookingsByStudio(studio.getId());

        // DatePicker con giorni occupati
        datePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker picker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        LocalDate now = LocalDate.now();
                        setDisable(empty || date.isBefore(now) || date.isAfter(now.plusMonths(1)));

                        boolean hasBooking = existingBookings.stream()
                                .anyMatch(b -> b.getStartTime().toLocalDate().equals(date));
                        if (hasBooking) {
                            setStyle("-fx-background-color: #ffcccc;");
                            setTooltip(new Tooltip("Giorno già occupato"));
                        }
                    }
                };
            }
        });
        datePicker.setValue(LocalDate.now());

        // Aggiorna fasce orarie quando cambia la data
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> updateTimeSlots(existingBookings, newDate));

        // Fasce orarie
        timeSlotComboBox.getItems().addAll("09:00 - 11:00", "11:00 - 13:00", "14:00 - 16:00", "16:00 - 18:00");
        timeSlotComboBox.getSelectionModel().selectFirst();

        loadEquipment();
    }

    public void setHomeController(HomeController homeController) {
        this.homeController = homeController;
    }

    private void loadEquipment() {
        List<Equipment> equipmentList = equipmentDAO.getEquipmentByStudio(studioSelezionato.getId());
        equipmentObservableList.setAll(equipmentList);
        equipmentListView.setItems(equipmentObservableList);

        // inizializza la mappa di selezione
        for (Equipment e : equipmentList) {
            equipmentSelectionMap.put(e, new SimpleBooleanProperty(false));
        }

        // CheckBoxListCell compatibile con JavaFX 17+
        equipmentListView.setCellFactory(lv -> new CheckBoxListCell<Equipment>(item -> {
            BooleanProperty selected = equipmentSelectionMap.get(item);
            selected.addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    if (!selectedEquipment.contains(item)) selectedEquipment.add(item);
                } else {
                    selectedEquipment.remove(item);
                }
            });
            return selected;
        }) {
            @Override
            public void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.getName());
                } else {
                    setText(null);
                }
            }
        });
    }

    private void updateTimeSlots(List<Booking> existingBookings, LocalDate date) {
        if (date == null) return;

        ObservableList<String> availableSlots = FXCollections.observableArrayList(
                "09:00 - 11:00",
                "11:00 - 13:00",
                "14:00 - 16:00",
                "16:00 - 18:00"
        );

        for (Booking b : existingBookings) {
            if (b.getStartTime().toLocalDate().equals(date)) {
                String slot = b.getStartTime().toLocalTime() + " - " + b.getEndTime().toLocalTime();
                availableSlots.remove(slot);
            }
        }

        timeSlotComboBox.setItems(availableSlots);
        if (!availableSlots.isEmpty()) timeSlotComboBox.getSelectionModel().selectFirst();
        else timeSlotComboBox.getSelectionModel().clearSelection();
    }

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

        if (date == null || timeSlot == null) { showAlert(Alert.AlertType.WARNING, "Seleziona giorno e fascia oraria."); return; }
        if (selectedEquipment.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Seleziona almeno un pezzo di attrezzatura."); return; }
        if (!checkRequiredEquipment(selectedEquipment)) return;

        String[] times = timeSlot.split(" - ");
        LocalTime startTime = LocalTime.parse(times[0].trim());
        LocalTime endTime = LocalTime.parse(times[1].trim());

        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        if (!bookingDAO.isAvailable(studioSelezionato.getId(), startDateTime, endDateTime)) {
            showAlert(Alert.AlertType.ERROR, "Lo studio non è disponibile in questa fascia oraria.");
            return;
        }

        boolean success = bookingDAO.createBooking(
                utenteLoggato.getId(),
                studioSelezionato.getId(),
                startDateTime,
                endDateTime,
                selectedEquipment
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
}
