package com.example.studiobooking.model;

public class Studio {
    private long id;
    private String name;
    private String description;
    private boolean isActive;

    public Studio(long id, String name, String description, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
    }

    // Getter e setter
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return name + (isActive ? " (Attivo)" : " (Non Attivo)");
    }
}
