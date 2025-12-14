package com.pomodoro.repository;

import com.pomodoro.model.DailyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing daily tasks
 */
@Repository
public interface DailyTaskRepository extends JpaRepository<DailyTask, Long> {
    
    /**
     * Find all tasks for a specific date, ordered by priority and creation time
     */
    @Query("SELECT t FROM DailyTask t WHERE t.taskDate = :date ORDER BY t.priority DESC, t.completed ASC, t.createdAt ASC")
    List<DailyTask> findTasksForDate(@Param("date") LocalDate date);
    
    /**
     * Find all incomplete tasks for a specific date
     */
    @Query("SELECT t FROM DailyTask t WHERE t.taskDate = :date AND t.completed = false ORDER BY t.priority DESC, t.createdAt ASC")
    List<DailyTask> findIncompleteTasksForDate(@Param("date") LocalDate date);
    
    /**
     * Find all completed tasks for a specific date
     */
    @Query("SELECT t FROM DailyTask t WHERE t.taskDate = :date AND t.completed = true ORDER BY t.completedAt DESC")
    List<DailyTask> findCompletedTasksForDate(@Param("date") LocalDate date);
    
    /**
     * Find tasks between two dates (for analytics)
     */
    @Query("SELECT t FROM DailyTask t WHERE t.taskDate >= :startDate AND t.taskDate <= :endDate ORDER BY t.taskDate DESC, t.priority DESC")
    List<DailyTask> findTasksBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * Count completed tasks for a specific date
     */
    @Query("SELECT COUNT(t) FROM DailyTask t WHERE t.taskDate = :date AND t.completed = true")
    long countCompletedTasksForDate(@Param("date") LocalDate date);
    
    /**
     * Count total tasks for a specific date
     */
    @Query("SELECT COUNT(t) FROM DailyTask t WHERE t.taskDate = :date")
    long countTasksForDate(@Param("date") LocalDate date);
    
    /**
     * Find overdue tasks (incomplete tasks from previous days)
     */
    @Query("SELECT t FROM DailyTask t WHERE t.taskDate < :currentDate AND t.completed = false ORDER BY t.taskDate DESC, t.priority DESC")
    List<DailyTask> findOverdueTasks(@Param("currentDate") LocalDate currentDate);
    
    /**
     * Delete tasks older than a specified date (for cleanup)
     */
    void deleteByTaskDateBefore(LocalDate cutoffDate);
}