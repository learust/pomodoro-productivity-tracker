package com.pomodoro.repository;

import com.pomodoro.model.CompletedSession;
import com.pomodoro.model.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for managing completed Pomodoro sessions
 */
@Repository
public interface CompletedSessionRepository extends JpaRepository<CompletedSession, Long> {
    
    /**
     * Find sessions by session type
     */
    List<CompletedSession> findBySessionType(SessionType sessionType);
    
    /**
     * Find sessions within a date range
     */
    @Query("SELECT s FROM CompletedSession s WHERE s.startTime >= :startDate AND s.startTime < :endDate ORDER BY s.startTime DESC")
    List<CompletedSession> findSessionsBetween(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find work sessions within a date range (for productivity tracking)
     */
    @Query("SELECT s FROM CompletedSession s WHERE s.sessionType = :sessionType AND s.startTime >= :startDate AND s.startTime < :endDate ORDER BY s.startTime DESC")
    List<CompletedSession> findWorkSessionsBetween(@Param("sessionType") SessionType sessionType,
                                                 @Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get total work hours for a specific date range
     */
    @Query("SELECT COALESCE(SUM(s.durationSeconds), 0) FROM CompletedSession s WHERE s.sessionType = 'WORK' AND s.startTime >= :startDate AND s.startTime < :endDate")
    Long getTotalWorkSecondsInRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find sessions for a specific date (for daily tracking)
     */
    @Query("SELECT s FROM CompletedSession s WHERE DATE(s.startTime) = DATE(:date) ORDER BY s.startTime DESC")
    List<CompletedSession> findSessionsForDate(@Param("date") LocalDateTime date);
    
    /**
     * Get all sessions ordered by most recent first
     */
    List<CompletedSession> findAllByOrderByStartTimeDesc();
}