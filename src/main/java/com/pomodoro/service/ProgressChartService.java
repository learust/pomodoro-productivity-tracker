package com.pomodoro.service;

import com.pomodoro.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for generating GitHub-style progress charts
 * Based on Pomodoro session data
 */
@Service
public class ProgressChartService {
    
    private final SessionLoggingService sessionLoggingService;
    
    @Autowired
    public ProgressChartService(SessionLoggingService sessionLoggingService) {
        this.sessionLoggingService = sessionLoggingService;
    }
    
    /**
     * Generate complete progress chart for a given year
     */
    public ProgressChart generateProgressChart(int year) {
        List<ProgressMonth> months = new ArrayList<>();
        
        for (Month month : Month.values()) {
            ProgressMonth progressMonth = generateProgressMonth(year, month);
            months.add(progressMonth);
        }
        
        return new ProgressChart(year, months);
    }
    
    /**
     * Generate progress chart for a specific month
     */
    public ProgressMonth generateProgressMonth(int year, Month month) {
        List<ProgressDay> days = new ArrayList<>();
        
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();
        
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            ProgressDay progressDay = generateProgressDay(date);
            days.add(progressDay);
        }
        
        return new ProgressMonth(year, month, days);
    }
    
    /**
     * Generate progress day data for a specific date
     */
    public ProgressDay generateProgressDay(LocalDate date) {
        double totalHours = sessionLoggingService.getTotalWorkHoursForDate(date);
        int sessionCount = sessionLoggingService.getWorkSessionCountForDate(date);
        int productivityLevel = sessionLoggingService.getProductivityLevelForDate(date);
        
        return new ProgressDay(date, totalHours, sessionCount, productivityLevel);
    }
    
    /**
     * Get available years for dropdown
     * Returns years that have session data, starting from 2025
     */
    public List<Integer> getAvailableYears() {
        List<Integer> years = new ArrayList<>();
        
        // Start from 2025 as specified in product-reference
        int currentYear = LocalDate.now().getYear();
        int startYear = Math.max(2025, currentYear - 2); // Go back 2 years max
        
        for (int year = startYear; year <= currentYear; year++) {
            years.add(year);
        }
        
        // If no years yet, add current year
        if (years.isEmpty()) {
            years.add(currentYear);
        }
        
        return years;
    }
    
    /**
     * Get progress chart for current year
     */
    public ProgressChart getCurrentYearProgressChart() {
        return generateProgressChart(LocalDate.now().getYear());
    }
    
    /**
     * Get productivity statistics for a year
     */
    public YearlyStats getYearlyStats(int year) {
        ProgressChart chart = generateProgressChart(year);
        
        return new YearlyStats(
            year,
            chart.getTotalYearHours(),
            chart.getTotalYearSessions(),
            chart.getTotalWorkDays(),
            chart.getAverageHoursPerWorkDay(),
            chart.getCurrentStreak(),
            chart.getLongestStreak()
        );
    }
    
    /**
     * Data class for yearly statistics
     */
    public static class YearlyStats {
        private int year;
        private double totalHours;
        private int totalSessions;
        private int workDays;
        private double averageHoursPerDay;
        private int currentStreak;
        private int longestStreak;
        
        public YearlyStats(int year, double totalHours, int totalSessions, int workDays, 
                          double averageHoursPerDay, int currentStreak, int longestStreak) {
            this.year = year;
            this.totalHours = totalHours;
            this.totalSessions = totalSessions;
            this.workDays = workDays;
            this.averageHoursPerDay = averageHoursPerDay;
            this.currentStreak = currentStreak;
            this.longestStreak = longestStreak;
        }
        
        // Getters
        public int getYear() { return year; }
        public double getTotalHours() { return totalHours; }
        public int getTotalSessions() { return totalSessions; }
        public int getWorkDays() { return workDays; }
        public double getAverageHoursPerDay() { return averageHoursPerDay; }
        public int getCurrentStreak() { return currentStreak; }
        public int getLongestStreak() { return longestStreak; }
        
        // Setters
        public void setYear(int year) { this.year = year; }
        public void setTotalHours(double totalHours) { this.totalHours = totalHours; }
        public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
        public void setWorkDays(int workDays) { this.workDays = workDays; }
        public void setAverageHoursPerDay(double averageHoursPerDay) { this.averageHoursPerDay = averageHoursPerDay; }
        public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
        public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
    }
}