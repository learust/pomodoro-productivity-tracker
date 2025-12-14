package com.pomodoro.controller;

import com.pomodoro.model.*;
import com.pomodoro.service.ProgressChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

/**
 * REST controller for progress chart functionality
 * Provides GitHub-style contribution chart data
 */
@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressChartController {

    private final ProgressChartService progressChartService;

    @Autowired
    public ProgressChartController(ProgressChartService progressChartService) {
        this.progressChartService = progressChartService;
    }

    /**
     * Get complete progress chart for a specific year
     */
    @GetMapping("/chart/{year}")
    public ResponseEntity<ProgressChart> getProgressChart(@PathVariable int year) {
        ProgressChart chart = progressChartService.generateProgressChart(year);
        return ResponseEntity.ok(chart);
    }

    /**
     * Get progress chart for current year
     */
    @GetMapping("/chart")
    public ResponseEntity<ProgressChart> getCurrentYearChart() {
        ProgressChart chart = progressChartService.getCurrentYearProgressChart();
        return ResponseEntity.ok(chart);
    }

    /**
     * Get progress data for a specific month
     */
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<ProgressMonth> getProgressMonth(
            @PathVariable int year, 
            @PathVariable int month) {
        Month monthEnum = Month.of(month);
        ProgressMonth progressMonth = progressChartService.generateProgressMonth(year, monthEnum);
        return ResponseEntity.ok(progressMonth);
    }

    /**
     * Get progress data for a specific day
     */
    @GetMapping("/day/{date}")
    public ResponseEntity<ProgressDay> getProgressDay(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ProgressDay progressDay = progressChartService.generateProgressDay(date);
        return ResponseEntity.ok(progressDay);
    }

    /**
     * Get available years for dropdown selection
     */
    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getAvailableYears() {
        List<Integer> years = progressChartService.getAvailableYears();
        return ResponseEntity.ok(years);
    }

    /**
     * Get yearly statistics summary
     */
    @GetMapping("/stats/{year}")
    public ResponseEntity<ProgressChartService.YearlyStats> getYearlyStats(@PathVariable int year) {
        ProgressChartService.YearlyStats stats = progressChartService.getYearlyStats(year);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get current year's statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ProgressChartService.YearlyStats> getCurrentYearStats() {
        int currentYear = LocalDate.now().getYear();
        ProgressChartService.YearlyStats stats = progressChartService.getYearlyStats(currentYear);
        return ResponseEntity.ok(stats);
    }
}