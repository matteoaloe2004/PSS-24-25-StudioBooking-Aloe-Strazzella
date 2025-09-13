package com.example.studiobooking.model;

import java.time.LocalDateTime;

public class Booking {
    private long id;
    private long userId;
    private long studioId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public Booking(long id, long userId, long studioId, LocalDateTime startTime, LocalDateTime endTime, String status) {
        this.id = id;
        this.userId = userId;
        this.studioId = studioId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getStudioId() { return studioId; }
    public void setStudioId(long studioId) { this.studioId = studioId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
