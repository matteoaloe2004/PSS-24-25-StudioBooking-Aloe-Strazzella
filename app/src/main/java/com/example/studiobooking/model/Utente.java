package com.example.studiobooking.model;

import java.sql.Timestamp;

public class Utente {
    private long id;
    private String username;      // nome dell'utente
    private String email;
    private String password;      // password hash o in chiaro per il DAO
    private boolean admin;        // true se admin
    private Timestamp createdAt;  // data di creazione
    private LoyaltyCard loyaltyCard;

    // Costruttore completo
    public Utente(long id, String username, String email, String password, Timestamp createdAt, boolean admin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.admin = admin;
    }

    // GETTER
    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getName() { return username; } // richiesto dai controller
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Timestamp getCreatedAt() { return createdAt; }
    public boolean isAdmin() { return admin; }
    public LoyaltyCard getLoyaltyCard() { return loyaltyCard; }

    // SETTER
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setAdmin(boolean admin) { this.admin = admin; }
    public void setLoyaltyCard(LoyaltyCard loyaltyCard) { this.loyaltyCard = loyaltyCard; }
}
