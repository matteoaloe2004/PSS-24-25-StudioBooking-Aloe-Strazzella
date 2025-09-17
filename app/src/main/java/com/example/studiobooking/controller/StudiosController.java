package com.example.studiobooking.controller;

import java.util.List;

import com.example.studiobooking.dao.StudioDAO;
import com.example.studiobooking.model.Studio;
import com.example.studiobooking.model.Utente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class StudiosController {

    @FXML
    private ListView<Studio> studiosListView;

    @FXML
    private Button bookButton;

    @FXML
    private Label welcomeLabel;

    private final StudioDAO studioDAO = new StudioDAO();
    private final ObservableList<Studio> studioObservableList = FXCollections.observableArrayList();

    private Utente utenteLoggato;

    @FXML
    public void initialize() {
        loadStudios();

        bookButton.setOnAction(e -> bookSelectedStudio());
    }

    // Metodo per impostare l'utente loggato
    public void setUtenteLoggato(Utente utente) {
        this.utenteLoggato = utente;
        if (welcomeLabel != null && utente != null) {
            welcomeLabel.setText("Benvenuto, " + utente.getName() + "!");
        }
    }

    // Carica gli studi attivi dal database
    private void loadStudios() {
        List<Studio> studios = studioDAO.getActiveStudios();
        studioObservableList.setAll(studios);
        studiosListView.setItems(studioObservableList);
    }

    // Gestione bottone prenota studio
    private void bookSelectedStudio() {
        Studio selected = studiosListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Seleziona uno studio prima di prenotare.");
            alert.showAndWait();
            return;
        }

        if (utenteLoggato == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Utente non loggato.");
            alert.showAndWait();
            return;
        }

        // Per ora mostra solo un alert di conferma
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Prenotazione per: " + selected.getName() + "\nUtente: " + utenteLoggato.getName());
        alert.showAndWait();

    }
}
