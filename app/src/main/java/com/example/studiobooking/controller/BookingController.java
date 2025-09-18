package com.example.studiobooking.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.example.studiobooking.dao.BookingDAO;
import com.example.studiobooking.dao.EquipmentDAO;
import com.example.studiobooking.model.Equipment;
import com.example.studiobooking.model.Studio;
import com.example.studiobooking.model.Utente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;

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

    public void initBooking(Utente utente, Studio studio) {
        this.utenteLoggato = utente;
        this.studioSelezionato = studio;

        studioLabel.setText("Prenotazione: " + studio.getName());
        studioDescriptionLabel.setText(studio.getDescription());

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate now = LocalDate.now();
                setDisable(empty || date.isBefore(now) || date.isAfter(now.plusMonths(1)));
            }
        });
        datePicker.setValue(LocalDate.now());

        timeSlotComboBox.getItems().addAll(
                "09:00 - 11:00",
                "11:00 - 13:00",
                "14:00 - 16:00",
                "16:00 - 18:00"
        );
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
        equipmentListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        equipmentListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Equipment item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    public void initialize() {
    confirmButton.setOnAction(e -> confirmBooking());
    backButton.setOnAction(e -> goBackToHome());
}

private void goBackToHome() {
    Stage stage = (Stage) backButton.getScene().getWindow();
    stage.close();
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
    LocalTime startTime = LocalTime.parse(times[0]);
    LocalTime endTime = LocalTime.parse(times[1]);

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

        // Aggiorna la lista delle prenotazioni e la loyalty card nella Home
        if (homeController != null) {
            homeController.loadUserBookings();   // aggiorna la lista prenotazioni
            homeController.loadLoyaltyCard();    // aggiorna i label loyalty
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