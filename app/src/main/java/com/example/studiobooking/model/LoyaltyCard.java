package com.example.studiobooking.model;

public class LoyaltyCard {
    private long id;
    private long userId;
    private int totalHours;
    private int discountLevel;

    public LoyaltyCard(long id, long userId, int totalHours, int discountLevel) {
        this.id = id;
        this.userId = userId;
        this.totalHours = totalHours;
        this.discountLevel = discountLevel;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public int getTotalHours() { return totalHours; }
    public void setTotalHours(int totalHours) { this.totalHours = totalHours; }
    public int getDiscountLevel() { return discountLevel; }
    public void setDiscountLevel(int discountLevel) { this.discountLevel = discountLevel; }
}
