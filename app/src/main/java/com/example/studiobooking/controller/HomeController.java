package com.example.studiobooking.controller;

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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private Button loginButton, registerButton, bookButton, logoutButton, cancelBookingButton;
    @FXML private ListView<Studio> studiosListView;
    @FXML private ListView<Booking> userBookingsListView;
    @FXML private VBox loyaltyCardInclude;

    private LoyaltyCardController loyaltyCardController;
    private final StudioDAO studioDAO = new StudioDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final LoyaltyCardDAO loyaltyCardDAO = new LoyaltyCardDAO();

    private final ObservableList<Studio> studioObservableList = FXCollections.observableArrayList();
    private final ObservableList<Booking> userBookingsObservableList = FXCollections.observableArrayList();
    private Utente utenteLoggato;

    @FXML
    public void initialize() {
        loadStudios();
        setupButtons();
        setupListViews();
        loadLoyaltyCardComponent();
    }

    private void setupButtons() {
        loginButton.setOnAction(e -> openLogin());
        registerButton.setOnAction(e -> openRegister());
        bookButton.setOnAction(e -> openBooking());
        logoutButton.setOnAction(e -> logout());
        cancelBookingButton.setOnAction(e -> cancelSelectedBooking());

        logoutButton.setVisible(false);
        cancelBookingButton.setDisable(true);
    }

    private void setupListViews() {
        studiosListView.setItems(studioObservableList);
        userBookingsListView.setItems(userBookingsObservableList);

        userBookingsListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSel, newSel) -> cancelBookingButton.setDisable(newSel == null || !canCancel(newSel))
        );
    }

    private void loadLoyaltyCardComponent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoyaltyCardView.fxml"));
            Parent root = loader.load();
            loyaltyCardInclude.getChildren().add(root);
            loyaltyCardController = loader.getController();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Errore nel caricamento della scheda fedeltà.");
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
            initOrLoadLoyaltyCard();
        } else {
            welcomeLabel.setText("Benvenuto su Studio Booking!");
            loginButton.setVisible(true);
            registerButton.setVisible(true);
            logoutButton.setVisible(false);
            userBookingsObservableList.clear();
            if (loyaltyCardController != null) loyaltyCardController.setUtente(null);
        }
    }

    public void loadLoyaltyCard() {
        if (utenteLoggato == null) return;

        // Recupera loyalty card; se non esiste la crea
        loyaltyCard = loyaltyCardDAO.getLoyaltyCardByUserId(utenteLoggato.getId());
        if (loyaltyCard == null) {
            loyaltyCardDAO.createLoyaltyCard(utenteLoggato.getId());
            loyaltyCard = loyaltyCardDAO.getLoyaltyCardByUserId(utenteLoggato.getId());
        }

        if (loyaltyCard != null) {
            // Aggiorna in tempo reale
            int totalBookings = bookingDAO.getBookingsByUser(utenteLoggato.getId()).size();
            loyaltyCardDAO.updateDiscountLevel(utenteLoggato.getId(), totalBookings);

            totalBookingsLabel.setText("Prenotazioni totali: " + totalBookings);
            discountLabel.setText("Sconto attuale: " + Math.min((totalBookings / 3) * 5, 30) + "%");
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

        if (loyaltyCardController != null && utenteLoggato.getLoyaltyCard() != null) {
            loyaltyCardController.updateLoyaltyInfo();
        }
    }

    private boolean canCancel(Booking booking) {
        return booking.getStartTime().isAfter(LocalDateTime.now().plusHours(24)) &&
               !"CANCELLED".equalsIgnoreCase(booking.getStatus());
    }

    private void cancelSelectedBooking() {
        Booking selectedBooking = userBookingsListView.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Seleziona una prenotazione da annullare.");
            return;
        }

        if (bookingDAO.cancelBooking(selectedBooking.getId())) {
            showAlert(Alert.AlertType.INFORMATION, "Prenotazione annullata con successo.");
            loadUserBookings();
        } else {
            showAlert(Alert.AlertType.ERROR, "Non puoi annullare questa prenotazione (deve essere almeno 24 ore prima).");
        }
    }

    private void logout() {
        setUtenteLoggato(null);
    }

    private void loadStudios() {
        studioObservableList.setAll(studioDAO.getActiveStudios());
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
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Errore nell'apertura della finestra di login.");
            ex.printStackTrace();
        }
    }

    private void openRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegisterView.fxml"));
            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(loader.load(), 400, 300));
            stage.setTitle("Registrazione");
            stage.show();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Errore nell'apertura della finestra di registrazione.");
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
            controller.setHomeController(this);

            Stage stage = new Stage();
            stage.setScene(new javafx.scene.Scene(root, 800, 600));
            stage.setTitle("Prenotazione Studio: " + selected.getName());
            stage.show();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Errore nell'apertura della finestra di prenotazione.");
            ex.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }

    public void loadLoyaltyCard() {
        if (loyaltyCardController != null && utenteLoggato != null) {
            loyaltyCardController.updateLoyaltyInfo();
        }
    }
}
