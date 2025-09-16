package com.example.studiobooking.controller;

import com.example.studiobooking.model.Utente;
import com.example.studiobooking.model.LoyaltyCard;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LoyaltyCardController {

    @FXML private Label loyaltyBookingsLabel;
    @FXML private Label loyaltyDiscountLabel;

    private Utente utente;

    public void setUtente(Utente utente) {
        this.utente = utente;
        updateLoyaltyInfo();
    }

    public void updateLoyaltyInfo() {
        if (utente != null && utente.getLoyaltyCard() != null) {
            LoyaltyCard card = utente.getLoyaltyCard();
            loyaltyBookingsLabel.setText("Prenotazioni totali: " + card.getTotalBookings());
            loyaltyDiscountLabel.setText("Sconto: " + card.getDiscountLevel() + "%");
        } else {
            loyaltyBookingsLabel.setText("Prenotazioni totali: 0");
            loyaltyDiscountLabel.setText("Sconto: 0%");
        }
    }

    // Aggiunge una prenotazione alla card
    public void addBooking() {
        if (utente != null && utente.getLoyaltyCard() != null) {
            utente.getLoyaltyCard().addBooking();
            updateLoyaltyInfo();
        }
    }
}
