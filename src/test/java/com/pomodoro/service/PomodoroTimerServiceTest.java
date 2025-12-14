package com.pomodoro.service;

import com.pomodoro.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for PomodoroTimerService
 * Covers the testing plan from product-reference:
 * - Timer start correctly with configured duration
 * - Pause functionality freezes time
 * - Stop resets timer to initial state
 * - Timer countdown displays correctly
 * - Transitions between work and rest periods
 */
class PomodoroTimerServiceTest {

    private PomodoroTimerService timerService;
    
    @Mock
    private SessionLoggingService mockSessionLoggingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        timerService = new PomodoroTimerService(mockSessionLoggingService);
    }

    @Test
    @DisplayName("Timer should start correctly with configured duration")
    void testTimerStartsWithConfiguredDuration() {
        // Given - Default settings (25 minutes work session)
        PomodoroSettings settings = timerService.getSettings();
        assertEquals(25, settings.getWorkDurationMinutes());
        
        // When - Start timer
        TimerSession session = timerService.startTimer();
        
        // Then - Timer should be running with correct duration
        assertEquals(TimerState.RUNNING, session.getState());
        assertEquals(SessionType.WORK, session.getSessionType());
        assertEquals(1500, session.getTotalDurationSeconds()); // 25 * 60
        assertEquals(1500, session.getRemainingSeconds());
        assertNotNull(session.getStartTime());
    }

    @Test
    @DisplayName("Pause functionality should freeze time")
    void testPauseFreezeTime() throws InterruptedException {
        // Given - Running timer
        TimerSession session = timerService.startTimer();
        assertEquals(TimerState.RUNNING, session.getState());
        
        // Wait a bit for countdown
        Thread.sleep(100);
        
        // When - Pause timer
        session = timerService.pauseTimer();
        int remainingAfterPause = session.getRemainingSeconds();
        
        // Wait more time
        Thread.sleep(100);
        
        // Then - Time should be frozen
        assertEquals(TimerState.PAUSED, session.getState());
        session = timerService.getCurrentSession();
        assertEquals(remainingAfterPause, session.getRemainingSeconds());
    }

    @Test
    @DisplayName("Stop should reset timer to initial state")
    void testStopResetsTimer() {
        // Given - Running timer
        timerService.startTimer();
        TimerSession session = timerService.getCurrentSession();
        assertEquals(TimerState.RUNNING, session.getState());
        
        // When - Stop timer
        session = timerService.stopTimer();
        
        // Then - Timer should be reset to initial state
        assertEquals(TimerState.STOPPED, session.getState());
        assertEquals(1500, session.getTotalDurationSeconds());
        assertEquals(1500, session.getRemainingSeconds());
        assertNull(session.getStartTime());
        assertNull(session.getEndTime());
    }

    @Test
    @DisplayName("Timer countdown should display correctly")
    void testTimerCountdownDisplay() {
        // Given - Timer session
        TimerSession session = timerService.getCurrentSession();
        
        // Then - Initial state should be correct
        assertEquals(1500, session.getTotalDurationSeconds());
        assertEquals(1500, session.getRemainingSeconds());
        assertEquals(0, session.getElapsedSeconds());
        assertEquals(0.0, session.getProgressPercentage());
    }

    @Test
    @DisplayName("Transitions between work and rest periods should work correctly")
    void testTransitionBetweenPeriods() {
        // Given - Completed work session
        TimerSession workSession = timerService.startTimer();
        assertEquals(SessionType.WORK, workSession.getSessionType());
        
        timerService.completeSession();
        
        // When - Transition to next session
        TimerSession nextSession = timerService.transitionToNextSession();
        
        // Then - Should be a break session
        assertTrue(nextSession.getSessionType() == SessionType.SHORT_BREAK || 
                  nextSession.getSessionType() == SessionType.LONG_BREAK);
        assertEquals(TimerState.STOPPED, nextSession.getState());
        
        // Verify duration based on session type
        if (nextSession.getSessionType() == SessionType.SHORT_BREAK) {
            assertEquals(300, nextSession.getTotalDurationSeconds()); // 5 minutes
        } else if (nextSession.getSessionType() == SessionType.LONG_BREAK) {
            assertEquals(900, nextSession.getTotalDurationSeconds()); // 15 minutes
        }
    }

    @Test
    @DisplayName("Settings should be configurable")
    void testConfigurableSettings() {
        // Given - Custom settings
        PomodoroSettings customSettings = new PomodoroSettings(30, 10, 20, 3);
        
        // When - Update settings
        timerService.updateSettings(customSettings);
        
        // Then - Settings should be updated
        PomodoroSettings updatedSettings = timerService.getSettings();
        assertEquals(30, updatedSettings.getWorkDurationMinutes());
        assertEquals(10, updatedSettings.getShortBreakDurationMinutes());
        assertEquals(20, updatedSettings.getLongBreakDurationMinutes());
        assertEquals(3, updatedSettings.getLongBreakInterval());
    }

    @Test
    @DisplayName("Complete session should log to session service")
    void testCompleteSessionLogsToService() {
        // Given - Running timer
        timerService.startTimer();
        
        // When - Complete session
        timerService.completeSession();
        
        // Then - Should attempt to log session
        verify(mockSessionLoggingService, times(1)).logCompletedSession(any(TimerSession.class));
    }

    @Test
    @DisplayName("Reset should return to work session")
    void testResetReturnsToWorkSession() {
        // Given - Any session type
        timerService.transitionToNextSession(); // Move to break
        
        // When - Reset session
        TimerSession session = timerService.resetSession();
        
        // Then - Should be back to work session
        assertEquals(SessionType.WORK, session.getSessionType());
        assertEquals(TimerState.STOPPED, session.getState());
        assertEquals(0, session.getCompletedWorkSessions());
    }

    @Test
    @DisplayName("Long break interval should be respected")
    void testLongBreakInterval() {
        // Given - Settings with long break interval of 2
        PomodoroSettings settings = new PomodoroSettings(25, 5, 15, 2);
        timerService.updateSettings(settings);
        
        // When - Complete 2 work sessions
        TimerSession session = timerService.getCurrentSession();
        
        // First work session
        timerService.completeSession();
        session = timerService.transitionToNextSession();
        assertEquals(SessionType.SHORT_BREAK, session.getSessionType());
        
        // Second work session
        timerService.transitionToNextSession(); // Back to work
        timerService.completeSession();
        session = timerService.transitionToNextSession();
        
        // Then - Should be long break after 2 work sessions
        assertEquals(SessionType.LONG_BREAK, session.getSessionType());
    }
}