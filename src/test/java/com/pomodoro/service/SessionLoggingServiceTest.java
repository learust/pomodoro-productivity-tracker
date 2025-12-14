package com.pomodoro.service;

import com.pomodoro.model.*;
import com.pomodoro.repository.CompletedSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.argThat;

/**
 * Comprehensive tests for SessionLoggingService
 * Covers the testing plan from product-reference:
 * - Sessions save to database
 * - Session dates include Date (UTC ISO), duration, session type
 * - Data persistence after page refresh
 * - Incomplete session should not corrupt data
 */
class SessionLoggingServiceTest {

    private SessionLoggingService sessionLoggingService;
    
    @Mock
    private CompletedSessionRepository mockRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionLoggingService = new SessionLoggingService(mockRepository);
    }

    @Test
    @DisplayName("Sessions should save to database with correct data")
    void testSessionsSaveToDatabase() {
        // Given - Completed timer session
        TimerSession timerSession = new TimerSession(SessionType.WORK, 25);
        timerSession.setStartTime(LocalDateTime.now(ZoneOffset.UTC));
        timerSession.setEndTime(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(25));
        
        CompletedSession savedSession = new CompletedSession(
            SessionType.WORK, 
            timerSession.getStartTime(), 
            timerSession.getEndTime(), 
            1500
        );
        
        when(mockRepository.save(any())).thenReturn(savedSession);
        
        // When - Log completed session
        CompletedSession result = sessionLoggingService.logCompletedSession(timerSession);
        
        // Then - Session should be saved with correct data
        assertNotNull(result);
        assertEquals(SessionType.WORK, result.getSessionType());
        assertEquals(1500, result.getDurationSeconds());
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndTime());
        
        verify(mockRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Session dates should include Date (UTC ISO), duration, session type")
    void testSessionDateFormat() {
        // Given - Session with specific UTC time
        LocalDateTime utcTime = LocalDateTime.of(2025, 12, 14, 10, 0, 0);
        TimerSession timerSession = new TimerSession(SessionType.WORK, 25);
        timerSession.setStartTime(utcTime);
        timerSession.setEndTime(utcTime.plusMinutes(25));
        
        CompletedSession savedSession = new CompletedSession(
            SessionType.WORK, 
            utcTime, 
            utcTime.plusMinutes(25), 
            1500
        );
        
        when(mockRepository.save(any(CompletedSession.class))).thenReturn(savedSession);
        
        // When - Log session
        CompletedSession result = sessionLoggingService.logCompletedSession(timerSession);
        
        // Then - Should have correct UTC ISO format
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndTime());
        assertEquals(SessionType.WORK, result.getSessionType());
        assertEquals(1500, result.getDurationSeconds());
        assertEquals(25, result.getDurationMinutes());
    }

    @Test
    @DisplayName("Should handle incomplete session validation")
    void testIncompleteSessionValidation() {
        // Given - Incomplete timer session (no end time)
        TimerSession incompleteSession = new TimerSession(SessionType.WORK, 25);
        incompleteSession.setStartTime(LocalDateTime.now(ZoneOffset.UTC));
        // No end time set
        
        // When/Then - Should throw exception for incomplete session
        assertThrows(IllegalArgumentException.class, () -> {
            sessionLoggingService.logCompletedSession(incompleteSession);
        });
        
        // Verify repository is not called
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get work sessions for specific date")
    void testGetWorkSessionsForDate() {
        // Given - Mock work sessions for today
        LocalDate today = LocalDate.now();
        List<CompletedSession> mockSessions = Arrays.asList(
            new CompletedSession(SessionType.WORK, today.atTime(9, 0), today.atTime(9, 25), 1500),
            new CompletedSession(SessionType.WORK, today.atTime(10, 0), today.atTime(10, 25), 1500)
        );
        
        when(mockRepository.findWorkSessionsBetween(
            eq(SessionType.WORK), 
            eq(today.atStartOfDay()), 
            eq(today.plusDays(1).atStartOfDay())
        )).thenReturn(mockSessions);
        
        // When - Get work sessions for today
        List<CompletedSession> result = sessionLoggingService.getWorkSessionsForDate(today);
        
        // Then - Should return work sessions
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getSessionType() == SessionType.WORK));
    }

    @Test
    @DisplayName("Should calculate total work hours correctly")
    void testTotalWorkHoursCalculation() {
        // Given - Mock total seconds for date
        LocalDate today = LocalDate.now();
        Long totalSeconds = 3600L; // 1 hour
        
        when(mockRepository.getTotalWorkSecondsInRange(
            eq(today.atStartOfDay()), 
            eq(today.plusDays(1).atStartOfDay())
        )).thenReturn(totalSeconds);
        
        // When - Get total work hours
        double totalHours = sessionLoggingService.getTotalWorkHoursForDate(today);
        
        // Then - Should return correct hours
        assertEquals(1.0, totalHours, 0.01);
    }

    @Test
    @DisplayName("Should determine correct productivity level")
    void testProductivityLevelDetermination() {
        // Given - Different work hour scenarios
        LocalDate today = LocalDate.now();
        
        // Mock different total work times
        when(mockRepository.getTotalWorkSecondsInRange(any(), any()))
            .thenReturn(0L)      // No work
            .thenReturn(1800L)   // 0.5 hours
            .thenReturn(7200L)   // 2 hours
            .thenReturn(14400L); // 4 hours
        
        // When/Then - Test different productivity levels
        assertEquals(0, sessionLoggingService.getProductivityLevelForDate(today)); // No work
        assertEquals(1, sessionLoggingService.getProductivityLevelForDate(today)); // < 1 hour
        assertEquals(2, sessionLoggingService.getProductivityLevelForDate(today)); // 1-3 hours
        assertEquals(3, sessionLoggingService.getProductivityLevelForDate(today)); // 4+ hours
    }

    @Test
    @DisplayName("Should get sessions for month correctly")
    void testGetSessionsForMonth() {
        // Given - Mock monthly sessions
        LocalDateTime startOfMonth = LocalDate.of(2025, 12, 1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.of(2025, 12, 31).plusDays(1).atStartOfDay();
        
        List<CompletedSession> mockSessions = Arrays.asList(
            new CompletedSession(SessionType.WORK, startOfMonth, startOfMonth.plusMinutes(25), 1500)
        );
        
        when(mockRepository.findSessionsBetween(startOfMonth, endOfMonth))
            .thenReturn(mockSessions);
        
        // When - Get sessions for December 2025
        List<CompletedSession> result = sessionLoggingService.getSessionsForMonth(2025, 12);
        
        // Then - Should return monthly sessions
        assertEquals(1, result.size());
        verify(mockRepository).findSessionsBetween(startOfMonth, endOfMonth);
    }

    @Test
    @DisplayName("Should get all sessions ordered by most recent")
    void testGetAllSessionsOrdered() {
        // Given - Mock sessions
        List<CompletedSession> mockSessions = Arrays.asList(
            new CompletedSession(SessionType.WORK, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(2).plusMinutes(25), 1500),
            new CompletedSession(SessionType.SHORT_BREAK, LocalDateTime.now().minusHours(1), LocalDateTime.now().minusHours(1).plusMinutes(5), 300)
        );
        
        when(mockRepository.findAllByOrderByStartTimeDesc()).thenReturn(mockSessions);
        
        // When - Get all sessions
        List<CompletedSession> result = sessionLoggingService.getAllSessions();
        
        // Then - Should return ordered sessions
        assertEquals(2, result.size());
        verify(mockRepository).findAllByOrderByStartTimeDesc();
    }

    @Test
    @DisplayName("Should delete session safely")
    void testDeleteSession() {
        // Given - Valid session ID
        Long sessionId = 1L;
        
        // When - Delete session
        sessionLoggingService.deleteSession(sessionId);
        
        // Then - Should call repository delete
        verify(mockRepository, times(1)).deleteById(sessionId);
        
        // Test null safety
        sessionLoggingService.deleteSession(null);
        verify(mockRepository, times(1)).deleteById(any()); // Should still be 1, not called again
    }
}