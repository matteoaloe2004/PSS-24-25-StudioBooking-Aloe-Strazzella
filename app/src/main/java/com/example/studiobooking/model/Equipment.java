package com.example.studiobooking.model;

public class Equipment {
    private long id;
    private String name;
    private String description;
    private boolean isAvailable;
    private String type;  // nuova proprietà

    public Equipment(long id, String name, String description, boolean isAvailable, String type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isAvailable = isAvailable;
        this.type = type;
    }

    @Override
    public String toString() {
        return this.getName(); // visualizzazione nella ListView
    }

    // Getter e Setter
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
