package com.example.studiobooking.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class LoyaltyCard {

    private final long id;
    private final long userId;
    private final IntegerProperty totalBooking = new SimpleIntegerProperty(0);
    private final IntegerProperty discountLevel = new SimpleIntegerProperty(0);

    public LoyaltyCard(long id, long userId, int totalBooking, int discountLevel) {
        this.id = id;
        this.userId = userId;
        this.totalBooking.set(totalBooking);
        this.discountLevel.set(discountLevel);
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }

    public int getTotalBooking() { return totalBooking.get(); }
    public void setTotalBooking(int value) { totalBooking.set(value); }
    public IntegerProperty totalBookingProperty() { return totalBooking; }

    public int getDiscountLevel() { return discountLevel.get(); }
    public void setDiscountLevel(int value) { discountLevel.set(value); }
    public IntegerProperty discountLevelProperty() { return discountLevel; }
}