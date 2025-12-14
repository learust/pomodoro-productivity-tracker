package com.pomodoro.model;

import java.time.Month;
import java.util.List;

/**
 * Represents a month in the progress chart
 * Contains all days for that month with their productivity data
 */
public class ProgressMonth {
    private int year;
    private Month month;
    private List<ProgressDay> days;
    private double totalHours;
    private int totalSessions;
    
    public ProgressMonth() {}
    
    public ProgressMonth(int year, Month month, List<ProgressDay> days) {
        this.year = year;
        this.month = month;
        this.days = days;
        calculateTotals();
    }
    
    /**
     * Calculate total hours and sessions for the month
     */
    private void calculateTotals() {
        if (days != null) {
            this.totalHours = days.stream()
                .mapToDouble(ProgressDay::getTotalHours)
                .sum();
            this.totalSessions = days.stream()
                .mapToInt(ProgressDay::getSessionCount)
                .sum();
        }
    }
    
    // Getters and setters
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public Month getMonth() {
        return month;
    }
    
    public void setMonth(Month month) {
        this.month = month;
    }
    
    public List<ProgressDay> getDays() {
        return days;
    }
    
    public void setDays(List<ProgressDay> days) {
        this.days = days;
        calculateTotals();
    }
    
    public double getTotalHours() {
        return totalHours;
    }
    
    public int getTotalSessions() {
        return totalSessions;
    }
    
    /**
     * Get the number of days in this month
     */
    public int getDaysInMonth() {
        return days != null ? days.size() : 0;
    }
    
    /**
     * Get month name
     */
    public String getMonthName() {
        return month.name();
    }
    
    /**
     * Get short month name (first 3 letters)
     */
    public String getShortMonthName() {
        return month.name().substring(0, 3);
    }
}