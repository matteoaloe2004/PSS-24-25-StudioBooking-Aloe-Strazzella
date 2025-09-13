package com.example.studiobooking.controller;

import com.example.studiobooking.dao.StudioDAO;
import com.example.studiobooking.model.Studio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.util.List;

public class StudiosController {

    @FXML
    private ListView<Studio> studiosListView;

    @FXML
    private Button bookButton;

    private StudioDAO studioDAO = new StudioDAO();
    private ObservableList<Studio> studioObservableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadStudios();

        bookButton.setOnAction(e -> bookSelectedStudio());
    }

    private void loadStudios() {
        List<Studio> studios = studioDAO.getActiveStudios();
        studioObservableList.setAll(studios);
        studiosListView.setItems(studioObservableList);
    }

    private void bookSelectedStudio() {
        Studio selected = studiosListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Seleziona uno studio prima di prenotare.");
            alert.showAndWait();
            return;
        }

        // Qui puoi aprire un'altra finestra per scegliere data/ora e attrezzatura
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Prenotazione per: " + selected.getName());
        alert.showAndWait();
    }
}
