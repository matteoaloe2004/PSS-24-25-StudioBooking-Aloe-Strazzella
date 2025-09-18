package com.example.studiobooking.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.example.studiobooking.dao.BookingDAO;
import com.example.studiobooking.dao.LoyaltyCardDAO;
import com.example.studiobooking.dao.StudioDAO;
import com.example.studiobooking.model.Booking;
import com.example.studiobooking.model.LoyaltyCard;
import com.example.studiobooking.model.Studio;
import com.example.studiobooking.model.Utente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private Button loginButton, registerButton, bookButton, logoutButton, cancelBookingButton;
    @FXML private ListView<Studio> studiosListView;
    @FXML private ListView<Booking> userBookingsListView;

    @FXML private Label totalBookingsLabel;
    @FXML private Label discountLabel;

    private final StudioDAO studioDAO = new StudioDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final LoyaltyCardDAO loyaltyCardDAO = new LoyaltyCardDAO();

    private final ObservableList<Studio> studioObservableList = FXCollections.observableArrayList();
    private final ObservableList<Booking> userBookingsObservableList = FXCollections.observableArrayList();

    private Utente utenteLoggato;
    private LoyaltyCard loyaltyCard;

    @FXML
    public void initialize() {
        loadStudios();

        // Pulsanti
        loginButton.setOnAction(e -> openLogin());
        registerButton.setOnAction(e -> openRegister());
        bookButton.setOnAction(e -> openBooking());
        logoutButton.setOnAction(e -> logout());
        cancelBookingButton.setOnAction(e -> cancelSelectedBooking());

        logoutButton.setVisible(false);
        cancelBookingButton.setDisable(true);

        // Lista studi
        studiosListView.setItems(studioObservableList);
        studiosListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Studio studio, boolean empty) {
                super.updateItem(studio, empty);
                if (empty || studio == null) setText(null);
                else {
                    setText(studio.getName() + "\n" + studio.getDescription());
                    setStyle("-fx-font-size: 14px; -fx-padding: 6px;");
                }
            }
        });

        // Lista prenotazioni utente
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

        // Abilita/disabilita pulsante Annulla
        userBookingsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            cancelBookingButton.setDisable(newSel == null || !canCancel(newSel));
        });
    }

    public void setUtenteLoggato(Utente utente) {
        this.utenteLoggato = utente;
        if (utente != null) {
            welcomeLabel.setText("Ciao " + utente.getName() + ", bentornato!");
            loginButton.setVisible(false);
            registerButton.setVisible(false);
            logoutButton.setVisible(true);

            loadUserBookings();   // carica le prenotazioni
            refreshLoyaltyCard();    // carica la loyalty card e aggiorna i label
        }
    }

    public void loadLoyaltyCard() {
    if (utenteLoggato == null) return;

    // Aggiorna sempre la loyalty card basandosi SOLO sulle prenotazioni effettive (non CANCELLED)
    loyaltyCardDAO.refreshLoyaltyCard(utenteLoggato.getId());
    loyaltyCard = loyaltyCardDAO.getLoyaltyCardByUserId(utenteLoggato.getId());

    if (loyaltyCard != null) {
        totalBookingsLabel.setText("Prenotazioni totali: " + loyaltyCard.getTotalBooking());
        discountLabel.setText("Sconto attuale: " + loyaltyCard.getDiscountLevel() + "%");
    }
}

    private void refreshLoyaltyCard() {
    if (utenteLoggato == null) return;

    try {
        // Conta solo le prenotazioni effettive (non cancellate)
        int totalBookings = bookingDAO.getBookingsByUser(utenteLoggato.getId())
                                      .stream()
                                      .filter(b -> !"CANCELLED".equalsIgnoreCase(b.getStatus()))
                                      .toArray().length;

        // Aggiorna loyalty card
        loyaltyCardDAO.updateDiscountLevel(utenteLoggato.getId(), totalBookings);

        // Aggiorna i label in GUI
        totalBookingsLabel.setText("Prenotazioni totali: " + totalBookings);
        discountLabel.setText("Sconto attuale: " + Math.min((totalBookings / 3) * 5, 30) + "%");

    } catch (Exception e) {
        e.printStackTrace();
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
            // Aggiorna loyalty card dopo cancellazione
            loadLoyaltyCard();
        } else {
            showAlert(Alert.AlertType.ERROR, "Non puoi annullare questa prenotazione (deve essere almeno 24 ore prima).");
        }
    }

    private void logout() {
        utenteLoggato = null;
        welcomeLabel.setText("Benvenuto su Studio Booking!");
        loginButton.setVisible(true);
        registerButton.setVisible(true);
        logoutButton.setVisible(false);
        userBookingsObservableList.clear();
        totalBookingsLabel.setText("Prenotazioni totali: 0");
        discountLabel.setText("Sconto attuale: 0%");
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
            loginController.setHomeController(this); // passa HomeController al LoginController

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
            controller.setHomeController(this); // passa HomeController a BookingController

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Prenotazione Studio: " + selected.getName());
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }
}
