package com.pomodoro.service;

import com.pomodoro.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeUnit;

/**
 * Core service for managing Pomodoro timer functionality
 */
@Service
public class PomodoroTimerService {
    
    private TimerSession currentSession;
    private PomodoroSettings settings;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> timerTask;
    
    private final SessionLoggingService sessionLoggingService;
    
    @Autowired
    public PomodoroTimerService(SessionLoggingService sessionLoggingService) {
        this.sessionLoggingService = sessionLoggingService;
        this.settings = new PomodoroSettings(); // Default settings
        resetSession();
    }

    /**
     * Start the timer for the current session
     */
    public TimerSession startTimer() {
        if (currentSession.getState() == TimerState.STOPPED || 
            currentSession.getState() == TimerState.PAUSED) {
            
            currentSession.setState(TimerState.RUNNING);
            if (currentSession.getStartTime() == null) {
                currentSession.setStartTime(LocalDateTime.now(ZoneOffset.UTC));
            }
            
            startCountdown();
        }
        return currentSession;
    }

    /**
     * Pause the current timer session
     */
    public TimerSession pauseTimer() {
        if (currentSession.getState() == TimerState.RUNNING) {
            currentSession.setState(TimerState.PAUSED);
            stopCountdown();
        }
        return currentSession;
    }

    /**
     * Stop the timer and reset to initial state
     */
    public TimerSession stopTimer() {
        currentSession.setState(TimerState.STOPPED);
        currentSession.setRemainingSeconds(currentSession.getTotalDurationSeconds());
        currentSession.setStartTime(null);
        currentSession.setEndTime(null);
        stopCountdown();
        return currentSession;
    }

    /**
     * Complete the current session and transition to the next phase
     */
    public TimerSession completeSession() {
        currentSession.setState(TimerState.COMPLETED);
        currentSession.setEndTime(LocalDateTime.now(ZoneOffset.UTC));
        stopCountdown();
        
        // Log the completed session
        try {
            sessionLoggingService.logCompletedSession(currentSession);
        } catch (Exception e) {
            // Log error but don't break the timer flow
            System.err.println("Failed to log session: " + e.getMessage());
        }
        
        // Increment work session count if it was a work session
        if (currentSession.getSessionType() == SessionType.WORK) {
            currentSession.setCompletedWorkSessions(currentSession.getCompletedWorkSessions() + 1);
        }
        
        return currentSession;
    }

    /**
     * Transition to the next session type based on Pomodoro rules
     */
    public TimerSession transitionToNextSession() {
        SessionType nextSessionType = determineNextSessionType();
        int duration = getDurationForSessionType(nextSessionType);
        
        TimerSession newSession = new TimerSession(nextSessionType, duration);
        newSession.setCompletedWorkSessions(currentSession.getCompletedWorkSessions());
        
        this.currentSession = newSession;
        return currentSession;
    }

    /**
     * Reset session to initial work session state
     */
    public TimerSession resetSession() {
        stopCountdown();
        this.currentSession = new TimerSession(SessionType.WORK, settings.getWorkDurationMinutes());
        return currentSession;
    }

    /**
     * Get the current timer session
     */
    public TimerSession getCurrentSession() {
        return currentSession;
    }

    /**
     * Update timer settings
     */
    public void updateSettings(PomodoroSettings newSettings) {
        this.settings = newSettings;
        // If timer is stopped, update the current session duration
        if (currentSession.getState() == TimerState.STOPPED) {
            int newDuration = getDurationForSessionType(currentSession.getSessionType());
            currentSession.setTotalDurationSeconds(newDuration * 60);
            currentSession.setRemainingSeconds(newDuration * 60);
        }
    }

    /**
     * Get current settings
     */
    public PomodoroSettings getSettings() {
        return settings;
    }

    // Private helper methods
    
    private void startCountdown() {
        stopCountdown(); // Stop any existing timer
        
        timerTask = scheduler.scheduleAtFixedRate(() -> {
            if (currentSession.getState() == TimerState.RUNNING) {
                currentSession.setRemainingSeconds(currentSession.getRemainingSeconds() - 1);
                
                // Check if session is completed
                if (currentSession.getRemainingSeconds() <= 0) {
                    currentSession.setRemainingSeconds(0);
                    completeSession();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void stopCountdown() {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel(false);
            timerTask = null;
        }
    }

    private SessionType determineNextSessionType() {
        if (currentSession.getSessionType() == SessionType.WORK) {
            // After work, determine break type
            if (currentSession.getCompletedWorkSessions() % settings.getLongBreakInterval() == 0) {
                return SessionType.LONG_BREAK;
            } else {
                return SessionType.SHORT_BREAK;
            }
        } else {
            // After any break, return to work
            return SessionType.WORK;
        }
    }

    private int getDurationForSessionType(SessionType sessionType) {
        return switch (sessionType) {
            case WORK -> settings.getWorkDurationMinutes();
            case SHORT_BREAK -> settings.getShortBreakDurationMinutes();
            case LONG_BREAK -> settings.getLongBreakDurationMinutes();
        };
    }
}