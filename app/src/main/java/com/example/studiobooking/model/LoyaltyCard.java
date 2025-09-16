package com.example.studiobooking.model;

public class LoyaltyCard {
    private long id;
    private long userId;
    private int totalBookings; // numero di prenotazioni effettuate
    private int discountLevel; // sconto in %

    public LoyaltyCard() {
        this.totalBookings = 0;
        this.discountLevel = 0;
    }

    public LoyaltyCard(long userId, int totalBookings, int discountLevel) {
        this.userId = userId;
        this.totalBookings = totalBookings;
        this.discountLevel = discountLevel;
    }

    // getter e setter
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public int getTotalBookings() { return totalBookings; }
    public void setTotalBookings(int totalBookings) { this.totalBookings = totalBookings; }

    public int getDiscountLevel() { return discountLevel; }
    public void setDiscountLevel(int discountLevel) { this.discountLevel = discountLevel; }

    // aggiunge una prenotazione e aggiorna automaticamente lo sconto
    public void addBooking() {
        this.totalBookings++;
        updateDiscountLevel();
    }

    // logica sconto
    private void updateDiscountLevel() {
        if (totalBookings >= 10) discountLevel = 20;
        else if (totalBookings >= 5) discountLevel = 10;
        else discountLevel = 0;
    }
}
