package com.pomodoro.model;

/**
 * Represents the configuration settings for the Pomodoro timer
 */
public class PomodoroSettings {
    private int workDurationMinutes = 25; // Default 25 minutes
    private int shortBreakDurationMinutes = 5; // Default 5 minutes
    private int longBreakDurationMinutes = 15; // Default 15 minutes
    private int longBreakInterval = 4; // After how many work sessions to take a long break
    private boolean autoStartBreaks = false;
    private boolean autoStartPomodoros = false;

    public PomodoroSettings() {}

    public PomodoroSettings(int workDuration, int shortBreakDuration, int longBreakDuration, int longBreakInterval) {
        this.workDurationMinutes = workDuration;
        this.shortBreakDurationMinutes = shortBreakDuration;
        this.longBreakDurationMinutes = longBreakDuration;
        this.longBreakInterval = longBreakInterval;
    }

    // Getters and setters
    public int getWorkDurationMinutes() {
        return workDurationMinutes;
    }

    public void setWorkDurationMinutes(int workDurationMinutes) {
        this.workDurationMinutes = workDurationMinutes;
    }

    public int getShortBreakDurationMinutes() {
        return shortBreakDurationMinutes;
    }

    public void setShortBreakDurationMinutes(int shortBreakDurationMinutes) {
        this.shortBreakDurationMinutes = shortBreakDurationMinutes;
    }

    public int getLongBreakDurationMinutes() {
        return longBreakDurationMinutes;
    }

    public void setLongBreakDurationMinutes(int longBreakDurationMinutes) {
        this.longBreakDurationMinutes = longBreakDurationMinutes;
    }

    public int getLongBreakInterval() {
        return longBreakInterval;
    }

    public void setLongBreakInterval(int longBreakInterval) {
        this.longBreakInterval = longBreakInterval;
    }

    public boolean isAutoStartBreaks() {
        return autoStartBreaks;
    }

    public void setAutoStartBreaks(boolean autoStartBreaks) {
        this.autoStartBreaks = autoStartBreaks;
    }

    public boolean isAutoStartPomodoros() {
        return autoStartPomodoros;
    }

    public void setAutoStartPomodoros(boolean autoStartPomodoros) {
        this.autoStartPomodoros = autoStartPomodoros;
    }
}