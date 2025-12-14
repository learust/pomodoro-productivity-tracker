package com.pomodoro.model;

import java.util.List;

/**
 * Represents a complete year's progress chart data
 * Contains all 12 months with their productivity data
 */
public class ProgressChart {
    private int year;
    private List<ProgressMonth> months;
    private double totalYearHours;
    private int totalYearSessions;
    private int totalWorkDays; // Days with at least some work
    
    public ProgressChart() {}
    
    public ProgressChart(int year, List<ProgressMonth> months) {
        this.year = year;
        this.months = months;
        calculateYearTotals();
    }
    
    /**
     * Calculate total statistics for the year
     */
    private void calculateYearTotals() {
        if (months != null) {
            this.totalYearHours = months.stream()
                .mapToDouble(ProgressMonth::getTotalHours)
                .sum();
            this.totalYearSessions = months.stream()
                .mapToInt(ProgressMonth::getTotalSessions)
                .sum();
            this.totalWorkDays = (int) months.stream()
                .flatMap(month -> month.getDays().stream())
                .filter(day -> day.getProductivityLevel() > 0)
                .count();
        }
    }
    
    // Getters and setters
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public List<ProgressMonth> getMonths() {
        return months;
    }
    
    public void setMonths(List<ProgressMonth> months) {
        this.months = months;
        calculateYearTotals();
    }
    
    public double getTotalYearHours() {
        return totalYearHours;
    }
    
    public int getTotalYearSessions() {
        return totalYearSessions;
    }
    
    public int getTotalWorkDays() {
        return totalWorkDays;
    }
    
    /**
     * Get average hours per work day
     */
    public double getAverageHoursPerWorkDay() {
        return totalWorkDays > 0 ? totalYearHours / totalWorkDays : 0.0;
    }
    
    /**
     * Get work day streak (consecutive days with work)
     */
    public int getCurrentStreak() {
        if (months == null || months.isEmpty()) {
            return 0;
        }
        
        int streak = 0;
        // Start from the most recent day and count backwards
        for (int i = months.size() - 1; i >= 0; i--) {
            ProgressMonth month = months.get(i);
            List<ProgressDay> days = month.getDays();
            
            for (int j = days.size() - 1; j >= 0; j--) {
                ProgressDay day = days.get(j);
                if (day.getProductivityLevel() > 0) {
                    streak++;
                } else {
                    return streak;
                }
            }
        }
        return streak;
    }
    
    /**
     * Get the longest streak in the year
     */
    public int getLongestStreak() {
        if (months == null || months.isEmpty()) {
            return 0;
        }
        
        int longestStreak = 0;
        int currentStreak = 0;
        
        for (ProgressMonth month : months) {
            for (ProgressDay day : month.getDays()) {
                if (day.getProductivityLevel() > 0) {
                    currentStreak++;
                    longestStreak = Math.max(longestStreak, currentStreak);
                } else {
                    currentStreak = 0;
                }
            }
        }
        
        return longestStreak;
    }
}