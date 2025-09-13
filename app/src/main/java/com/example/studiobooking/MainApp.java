package com.example.studiobooking;

import com.example.studiobooking.controller.AuthController;
import com.example.studiobooking.model.Utente;

public class MainApp {

    public static void main(String[] args) {
        AuthController auth = new AuthController();

        // --- TEST REGISTRAZIONE ---
        System.out.println("Provo a registrare un nuovo utente...");
        boolean registered = auth.register("Test User", "testuser@example.com", "pw_hash_test");
        System.out.println("Registrazione: " + (registered ? "Riuscita" : "Fallita"));

        // --- TEST LOGIN ---
        System.out.println("Provo a fare login...");
        Utente utente = auth.login("testuser@example.com", "pw_hash_test");

        if (utente != null) {
            System.out.println("Login riuscito!");
            System.out.println("ID: " + utente.getId());
            System.out.println("Nome: " + utente.getName());
            System.out.println("Email: " + utente.getEmail());
        } else {
            System.out.println("Login fallito!");
        }
    }
}
