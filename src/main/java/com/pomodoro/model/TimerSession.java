package com.pomodoro.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Represents a timer session with its state and timing information
 */
public class TimerSession {
    private SessionType sessionType;
    private TimerState state;
    private int totalDurationSeconds;
    private int remainingSeconds;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime endTime;
    
    private int completedWorkSessions = 0;

    public TimerSession() {}

    public TimerSession(SessionType sessionType, int durationMinutes) {
        this.sessionType = sessionType;
        this.totalDurationSeconds = durationMinutes * 60;
        this.remainingSeconds = this.totalDurationSeconds;
        this.state = TimerState.STOPPED;
    }

    // Getters and setters
    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public TimerState getState() {
        return state;
    }

    public void setState(TimerState state) {
        this.state = state;
    }

    public int getTotalDurationSeconds() {
        return totalDurationSeconds;
    }

    public void setTotalDurationSeconds(int totalDurationSeconds) {
        this.totalDurationSeconds = totalDurationSeconds;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
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

    public int getCompletedWorkSessions() {
        return completedWorkSessions;
    }

    public void setCompletedWorkSessions(int completedWorkSessions) {
        this.completedWorkSessions = completedWorkSessions;
    }

    public boolean isCompleted() {
        return remainingSeconds <= 0;
    }

    public int getElapsedSeconds() {
        return totalDurationSeconds - remainingSeconds;
    }

    public double getProgressPercentage() {
        if (totalDurationSeconds == 0) return 0.0;
        return ((double) getElapsedSeconds() / totalDurationSeconds) * 100.0;
    }
}