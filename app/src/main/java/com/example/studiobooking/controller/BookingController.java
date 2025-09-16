package com.example.studiobooking.controller;

import com.example.studiobooking.dao.EquipmentDAO;
import com.example.studiobooking.dao.BookingDAO;
import com.example.studiobooking.model.Utente;
import com.example.studiobooking.model.Studio;
import com.example.studiobooking.model.Equipment;
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

    @FXML private Label studioLabel;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeSlotComboBox;
    @FXML private ListView<Equipment> equipmentListView;
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

    @FXML
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

            // Aggiorna la loyalty card con 1 prenotazione
            if (homeController != null) {
                homeController.addLoyaltyBooking();
                homeController.loadUserBookings();
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
