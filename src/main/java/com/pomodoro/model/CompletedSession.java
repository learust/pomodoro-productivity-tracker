package com.pomodoro.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a completed Pomodoro session
 * Used for tracking productivity and generating progress charts
 */
@Entity
@Table(name = "completed_sessions")
public class CompletedSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType sessionType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    @Column(nullable = false)
    private int durationSeconds;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    public CompletedSession() {
        this.createdAt = LocalDateTime.now();
    }
    
    public CompletedSession(SessionType sessionType, LocalDateTime startTime, LocalDateTime endTime, int durationSeconds) {
        this();
        this.sessionType = sessionType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationSeconds = durationSeconds;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public SessionType getSessionType() {
        return sessionType;
    }
    
    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public int getDurationSeconds() {
        return durationSeconds;
    }
    
    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper methods
    public int getDurationMinutes() {
        return durationSeconds / 60;
    }
    
    public double getDurationHours() {
        return durationSeconds / 3600.0;
    }
}