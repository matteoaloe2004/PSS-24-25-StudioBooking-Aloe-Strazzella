package com.example.studiobooking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Booking {
    private long id;
    private long userId;
    private String userName;
    private long studioId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public Booking(long id, long userId, String userName, long studioId,
                   LocalDateTime startTime, LocalDateTime endTime, String status) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.studioId = studioId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public long getStudioId() { return studioId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getStatus() { return status; }
    

    public void setUserName(String userName) { this.userName = userName; }
    public void setStatus(String status) { this.status = status; }

    

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return "Prenotazione #" + id +
                " | Utente: " + userName +
                " | Studio: " + studioId +
                " | " + startTime.format(fmt) + " → " + endTime.format(fmt) +
                " | Stato: " + status;
    }
}
