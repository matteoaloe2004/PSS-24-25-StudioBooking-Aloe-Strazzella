package com.example.studiobooking.model;

import java.sql.Timestamp;

public class Utente {
    private long id;
    private String name;
    private String email;
    private String password;
    private Timestamp createdAt;
    private boolean isAdmin;

    private LoyaltyCard loyaltyCard;

    public Utente(long id, String name, String email, String password, Timestamp createdAt, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.isAdmin = isAdmin;
        this.loyaltyCard = new LoyaltyCard();
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Timestamp getCreatedAt() { return createdAt; }
    public boolean isAdmin() { return isAdmin; }

    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }

    public LoyaltyCard getLoyaltyCard() { return loyaltyCard; }
    public void setLoyaltyCard(LoyaltyCard loyaltyCard) { this.loyaltyCard = loyaltyCard; }
}
