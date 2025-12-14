package com.pomodoro.service;

import com.pomodoro.model.CompletedSession;
import com.pomodoro.model.SessionType;
import com.pomodoro.model.TimerSession;
import com.pomodoro.repository.CompletedSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing completed Pomodoro sessions and productivity tracking
 */
@Service
public class SessionLoggingService {
    
    private final CompletedSessionRepository completedSessionRepository;
    
    @Autowired
    public SessionLoggingService(CompletedSessionRepository completedSessionRepository) {
        this.completedSessionRepository = completedSessionRepository;
    }
    
    /**
     * Log a completed session from a timer session
     */
    public CompletedSession logCompletedSession(TimerSession timerSession) {
        if (timerSession.getStartTime() == null || timerSession.getEndTime() == null) {
            throw new IllegalArgumentException("Session must have both start and end times to be logged");
        }
        
        CompletedSession session = new CompletedSession(
            timerSession.getSessionType(),
            timerSession.getStartTime(),
            timerSession.getEndTime(),
            timerSession.getElapsedSeconds()
        );
        
        return completedSessionRepository.save(session);
    }
    
    /**
     * Get all completed sessions
     */
    public List<CompletedSession> getAllSessions() {
        return completedSessionRepository.findAllByOrderByStartTimeDesc();
    }
    
    /**
     * Get sessions within a date range
     */
    public List<CompletedSession> getSessionsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return completedSessionRepository.findSessionsBetween(startDate, endDate);
    }
    
    /**
     * Get work sessions for a specific date (for daily productivity)
     */
    public List<CompletedSession> getWorkSessionsForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        return completedSessionRepository.findWorkSessionsBetween(
            SessionType.WORK, startOfDay, endOfDay);
    }
    
    /**
     * Get total work hours for a specific date
     */
    public double getTotalWorkHoursForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        Long totalSeconds = completedSessionRepository.getTotalWorkSecondsInRange(startOfDay, endOfDay);
        return totalSeconds / 3600.0; // Convert to hours
    }
    
    /**
     * Get work session count for a specific date
     */
    public int getWorkSessionCountForDate(LocalDate date) {
        return getWorkSessionsForDate(date).size();
    }
    
    /**
     * Get productivity level for a date based on hours worked
     * Returns: 0 = no work, 1 = <1hr, 2 = 1-3hr, 3 = 4+hr
     * This maps to the GitHub-style contribution chart colors
     */
    public int getProductivityLevelForDate(LocalDate date) {
        double hoursWorked = getTotalWorkHoursForDate(date);
        
        if (hoursWorked == 0) return 0;           // White/Black - no work
        else if (hoursWorked < 1) return 1;       // Dark green - < 1 hour
        else if (hoursWorked <= 3) return 2;      // Yellow - 1-3 hours  
        else return 3;                            // Bright green - 4+ hours
    }
    
    /**
     * Get productivity data for a month (for progress chart)
     */
    public List<CompletedSession> getSessionsForMonth(int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
        
        LocalDateTime startOfMonth = firstDay.atStartOfDay();
        LocalDateTime endOfMonth = lastDay.plusDays(1).atStartOfDay();
        
        return getSessionsBetween(startOfMonth, endOfMonth);
    }
    
    /**
     * Get productivity data for the entire year (for progress chart)
     */
    public List<CompletedSession> getSessionsForYear(int year) {
        LocalDateTime startOfYear = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime endOfYear = LocalDate.of(year + 1, 1, 1).atStartOfDay();
        
        return getSessionsBetween(startOfYear, endOfYear);
    }
    
    /**
     * Delete a completed session (if needed for data management)
     */
    public void deleteSession(Long sessionId) {
        if (sessionId != null) {
            completedSessionRepository.deleteById(sessionId);
        }
    }
}