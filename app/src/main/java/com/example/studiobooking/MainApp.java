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
        Parent root = FXMLLoader.load(getClass().getResource("/view/LoginView.fxml"));
        primaryStage.setTitle("Studio Booking - Login");
        primaryStage.setScene(new Scene(root, 400, 300)); // puoi adattare dimensioni
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
