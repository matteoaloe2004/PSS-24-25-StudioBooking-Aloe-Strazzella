package com.example.studiobooking.model;

public class LoyaltyCard {
    private long id;
    private long userId;
    private int totalBookings; // pluralizzato per coerenza
    private int discountLevel; // in percentuale

    public LoyaltyCard(long id, long userId, int totalBookings, int discountLevel) {
        this.id = id;
        this.userId = userId;
        this.totalBookings = totalBookings;
        this.discountLevel = discountLevel;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public int getTotalBookings() { return totalBookings; } // metodo coerente con controller
    public int getDiscountLevel() { return discountLevel; }

    public void setTotalBookings(int totalBookings) { this.totalBookings = totalBookings; }
    public void setDiscountLevel(int discountLevel) { this.discountLevel = discountLevel; }
}
