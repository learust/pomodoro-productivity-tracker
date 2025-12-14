package com.pomodoro.model;

import java.time.LocalDate;

/**
 * Represents a single day in the progress chart
 * Similar to GitHub contribution chart squares
 */
public class ProgressDay {
    private LocalDate date;
    private double totalHours;
    private int sessionCount;
    private int productivityLevel; // 0-3 for color coding
    
    public ProgressDay() {}
    
    public ProgressDay(LocalDate date, double totalHours, int sessionCount, int productivityLevel) {
        this.date = date;
        this.totalHours = totalHours;
        this.sessionCount = sessionCount;
        this.productivityLevel = productivityLevel;
    }
    
    // Getters and setters
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public double getTotalHours() {
        return totalHours;
    }
    
    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }
    
    public int getSessionCount() {
        return sessionCount;
    }
    
    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }
    
    public int getProductivityLevel() {
        return productivityLevel;
    }
    
    public void setProductivityLevel(int productivityLevel) {
        this.productivityLevel = productivityLevel;
    }
    
    /**
     * Get color class for this productivity level
     * Matches the product-reference color scheme
     */
    public String getColorClass() {
        return switch (productivityLevel) {
            case 0 -> "no-work";        // White/Black - no hours worked
            case 1 -> "light-work";     // Dark green - < 1 hour worked  
            case 2 -> "medium-work";    // Yellow - 1-3 hours of work
            case 3 -> "high-work";      // Bright green - 4+ hours worked
            default -> "no-work";
        };
    }
    
    /**
     * Get CSS color for this productivity level
     */
    public String getCssColor() {
        return switch (productivityLevel) {
            case 0 -> "#ebedf0";  // Light gray (GitHub style for no activity)
            case 1 -> "#9be9a8";  // Light green 
            case 2 -> "#40c463";  // Medium green
            case 3 -> "#30a14e";  // Dark green
            default -> "#ebedf0";
        };
    }
    
    /**
     * Get description for tooltip
     */
    public String getDescription() {
        if (productivityLevel == 0) {
            return "No work sessions";
        }
        return String.format("%.1f hours, %d sessions", totalHours, sessionCount);
    }
}