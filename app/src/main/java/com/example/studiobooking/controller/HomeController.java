package com.example.studiobooking.controller;

import com.example.studiobooking.dao.BookingDAO;
import com.example.studiobooking.dao.StudioDAO;
import com.example.studiobooking.model.Booking;
import com.example.studiobooking.model.Studio;
import com.example.studiobooking.model.Utente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.time.LocalDateTime;

public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private Button loginButton, registerButton, bookButton, logoutButton, cancelBookingButton;
    @FXML private ListView<Studio> studiosListView;
    @FXML private ListView<Booking> userBookingsListView;

    // Nodo root della loyalty card inclusa
    @FXML private VBox loyaltyCardInclude;

    // Controller reale della loyalty card
    private LoyaltyCardController loyaltyCardController;

    private StudioDAO studioDAO = new StudioDAO();
    private BookingDAO bookingDAO = new BookingDAO();

    private ObservableList<Studio> studioObservableList = FXCollections.observableArrayList();
    private ObservableList<Booking> userBookingsObservableList = FXCollections.observableArrayList();

    private Utente utenteLoggato;

    @FXML
    public void initialize() {
        loadStudios();

        loginButton.setOnAction(e -> openLogin());
        registerButton.setOnAction(e -> openRegister());
        bookButton.setOnAction(e -> openBooking());
        logoutButton.setOnAction(e -> logout());
        cancelBookingButton.setOnAction(e -> cancelSelectedBooking());

        logoutButton.setVisible(false);
        cancelBookingButton.setDisable(true);

        studiosListView.setItems(studioObservableList);
        studiosListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Studio studio, boolean empty) {
                super.updateItem(studio, empty);
                if (empty || studio == null) setText(null);
                else setText(studio.getName() + "\n" + studio.getDescription());
            }
        });

        userBookingsListView.setItems(userBookingsObservableList);
        userBookingsListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Booking booking, boolean empty) {
                super.updateItem(booking, empty);
                if (empty || booking == null) setText(null);
                else setText("Studio ID: " + booking.getStudioId() +
                        "\nData: " + booking.getStartTime().toLocalDate() +
                        "\nFascia: " + booking.getStartTime().toLocalTime() + " - " + booking.getEndTime().toLocalTime() +
                        "\nStato: " + booking.getStatus());
            }
        });

        userBookingsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            cancelBookingButton.setDisable(newSel == null || !canCancel(newSel));
        });

        // Carica il controller della loyalty card dal nodo incluso
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoyaltyCardView.fxml"));
            loader.setRoot(loyaltyCardInclude);
            loader.setControllerFactory(param -> new LoyaltyCardController());
            loader.load();
            loyaltyCardController = loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUtenteLoggato(Utente utente) {
        this.utenteLoggato = utente;
        if (utente != null) {
            welcomeLabel.setText("Ciao " + utente.getName() + ", bentornato!");
            loginButton.setVisible(false);
            registerButton.setVisible(false);
            logoutButton.setVisible(true);
            loadUserBookings();
            if (loyaltyCardController != null) {
                loyaltyCardController.setUtente(utente);
            }
        }
    }

    public void loadUserBookings() {
        if (utenteLoggato == null) return;
        List<Booking> bookings = bookingDAO.getBookingsByUser(utenteLoggato.getId());
        userBookingsObservableList.setAll(bookings);
        Booking selected = userBookingsListView.getSelectionModel().getSelectedItem();
        cancelBookingButton.setDisable(selected == null || !canCancel(selected));
    }

    private boolean canCancel(Booking booking) {
        LocalDateTime now = LocalDateTime.now();
        return booking.getStartTime().isAfter(now.plusHours(24)) && !"CANCELLED".equalsIgnoreCase(booking.getStatus());
    }

    private void cancelSelectedBooking() {
        Booking selectedBooking = userBookingsListView.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Seleziona una prenotazione da annullare.");
            return;
        }
        boolean success = bookingDAO.cancelBooking(selectedBooking.getId());
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Prenotazione annullata con successo.");
            userBookingsObservableList.remove(selectedBooking);
        } else {
            showAlert(Alert.AlertType.ERROR, "Non puoi annullare questa prenotazione (minimo 24h prima).");
        }
    }

    private void logout() {
        utenteLoggato = null;
        welcomeLabel.setText("Benvenuto su Studio Booking!");
        loginButton.setVisible(true);
        registerButton.setVisible(true);
        logoutButton.setVisible(false);
        userBookingsObservableList.clear();
        if (loyaltyCardController != null) loyaltyCardController.setUtente(null);
    }

    private void loadStudios() {
        List<Studio> studios = studioDAO.getActiveStudios();
        studioObservableList.setAll(studios);
    }

    private void openLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();
            LoginController loginController = loader.getController();
            loginController.setHomeController(this);

            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void openRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load(), 400, 300));
            stage.setTitle("Registrazione");
            stage.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void openBooking() {
        Studio selected = studiosListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Seleziona uno studio prima di prenotare.");
            return;
        }
        if (utenteLoggato == null) {
            showAlert(Alert.AlertType.ERROR, "Devi essere loggato per prenotare uno studio.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BookingView.fxml"));
            Parent root = loader.load();
            BookingController controller = loader.getController();
            controller.initBooking(utenteLoggato, selected);
            controller.setHomeController(this);

            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(root, 800, 600));
            stage.setTitle("Prenotazione Studio: " + selected.getName());
            stage.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }

    // Metodo pubblico per aggiungere una prenotazione alla loyalty card
    public void addLoyaltyBooking() {
        if (loyaltyCardController != null) {
            loyaltyCardController.addBooking();
        }
    }
}
