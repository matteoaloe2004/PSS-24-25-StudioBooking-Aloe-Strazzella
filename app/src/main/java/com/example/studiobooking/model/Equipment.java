package com.example.studiobooking.model;

public class Equipment {
    private long id;
    private String name;
    private String description;
    private boolean isAvailable;

    public Equipment(long id, String name, String description, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isAvailable = isAvailable;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
