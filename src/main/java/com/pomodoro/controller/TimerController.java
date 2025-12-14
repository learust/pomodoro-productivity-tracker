package com.pomodoro.controller;

import com.pomodoro.model.PomodoroSettings;
import com.pomodoro.model.TimerSession;
import com.pomodoro.service.PomodoroTimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Pomodoro timer operations
 */
@RestController
@RequestMapping("/api/timer")
@CrossOrigin(origins = "*") // For frontend integration
public class TimerController {

    private final PomodoroTimerService timerService;

    @Autowired
    public TimerController(PomodoroTimerService timerService) {
        this.timerService = timerService;
    }

    /**
     * Get current timer session status
     */
    @GetMapping("/status")
    public ResponseEntity<TimerSession> getTimerStatus() {
        return ResponseEntity.ok(timerService.getCurrentSession());
    }

    /**
     * Start the timer
     */
    @PostMapping("/start")
    public ResponseEntity<TimerSession> startTimer() {
        TimerSession session = timerService.startTimer();
        return ResponseEntity.ok(session);
    }

    /**
     * Pause the timer
     */
    @PostMapping("/pause")
    public ResponseEntity<TimerSession> pauseTimer() {
        TimerSession session = timerService.pauseTimer();
        return ResponseEntity.ok(session);
    }

    /**
     * Stop the timer and reset
     */
    @PostMapping("/stop")
    public ResponseEntity<TimerSession> stopTimer() {
        TimerSession session = timerService.stopTimer();
        return ResponseEntity.ok(session);
    }

    /**
     * Complete current session and transition to next
     */
    @PostMapping("/complete")
    public ResponseEntity<TimerSession> completeSession() {
        timerService.completeSession();
        TimerSession nextSession = timerService.transitionToNextSession();
        return ResponseEntity.ok(nextSession);
    }

    /**
     * Reset to initial work session
     */
    @PostMapping("/reset")
    public ResponseEntity<TimerSession> resetSession() {
        TimerSession session = timerService.resetSession();
        return ResponseEntity.ok(session);
    }

    /**
     * Get current timer settings
     */
    @GetMapping("/settings")
    public ResponseEntity<PomodoroSettings> getSettings() {
        return ResponseEntity.ok(timerService.getSettings());
    }

    /**
     * Update timer settings
     */
    @PutMapping("/settings")
    public ResponseEntity<PomodoroSettings> updateSettings(@RequestBody PomodoroSettings settings) {
        timerService.updateSettings(settings);
        return ResponseEntity.ok(timerService.getSettings());
    }
}