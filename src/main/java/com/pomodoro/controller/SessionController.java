package com.pomodoro.controller;

import com.pomodoro.model.CompletedSession;
import com.pomodoro.service.SessionLoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for session logging and productivity tracking
 */
@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class SessionController {

    private final SessionLoggingService sessionLoggingService;

    @Autowired
    public SessionController(SessionLoggingService sessionLoggingService) {
        this.sessionLoggingService = sessionLoggingService;
    }

    /**
     * Get all completed sessions
     */
    @GetMapping
    public ResponseEntity<List<CompletedSession>> getAllSessions() {
        return ResponseEntity.ok(sessionLoggingService.getAllSessions());
    }

    /**
     * Get work sessions for a specific date
     */
    @GetMapping("/work/{date}")
    public ResponseEntity<List<CompletedSession>> getWorkSessionsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(sessionLoggingService.getWorkSessionsForDate(date));
    }

    /**
     * Get productivity statistics for a specific date
     */
    @GetMapping("/stats/{date}")
    public ResponseEntity<ProductivityStats> getProductivityStats(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        double totalHours = sessionLoggingService.getTotalWorkHoursForDate(date);
        int sessionCount = sessionLoggingService.getWorkSessionCountForDate(date);
        int productivityLevel = sessionLoggingService.getProductivityLevelForDate(date);
        
        ProductivityStats stats = new ProductivityStats(date, totalHours, sessionCount, productivityLevel);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get sessions for a specific month (for progress chart)
     */
    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<CompletedSession>> getSessionsForMonth(
            @PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(sessionLoggingService.getSessionsForMonth(year, month));
    }

    /**
     * Get sessions for an entire year (for progress chart)
     */
    @GetMapping("/year/{year}")
    public ResponseEntity<List<CompletedSession>> getSessionsForYear(@PathVariable int year) {
        return ResponseEntity.ok(sessionLoggingService.getSessionsForYear(year));
    }

    /**
     * Delete a session by ID
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        sessionLoggingService.deleteSession(sessionId);
        return ResponseEntity.ok().build();
    }

    /**
     * Inner class for productivity statistics response
     */
    public static class ProductivityStats {
        private LocalDate date;
        private double totalHours;
        private int sessionCount;
        private int productivityLevel;

        public ProductivityStats(LocalDate date, double totalHours, int sessionCount, int productivityLevel) {
            this.date = date;
            this.totalHours = totalHours;
            this.sessionCount = sessionCount;
            this.productivityLevel = productivityLevel;
        }

        // Getters
        public LocalDate getDate() { return date; }
        public double getTotalHours() { return totalHours; }
        public int getSessionCount() { return sessionCount; }
        public int getProductivityLevel() { return productivityLevel; }

        // Setters
        public void setDate(LocalDate date) { this.date = date; }
        public void setTotalHours(double totalHours) { this.totalHours = totalHours; }
        public void setSessionCount(int sessionCount) { this.sessionCount = sessionCount; }
        public void setProductivityLevel(int productivityLevel) { this.productivityLevel = productivityLevel; }
    }
}