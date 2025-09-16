package com.example.studiobooking;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carica l'FXML della schermata di login dalla cartella resources/view
        Parent root = FXMLLoader.load(getClass().getResource("/view/HomeView.fxml"));
        primaryStage.setTitle("Studio Booking - Home");
        primaryStage.setScene(new Scene(root, 800, 600)); // puoi adattare dimensioni
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
