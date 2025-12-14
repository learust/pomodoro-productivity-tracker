package com.pomodoro.service;

import com.pomodoro.model.DailyTask;
import com.pomodoro.repository.DailyTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing daily tasks
 * Handles CRUD operations and daily task lifecycle
 */
@Service
@Transactional
public class TaskManagementService {
    
    private final DailyTaskRepository taskRepository;
    
    @Autowired
    public TaskManagementService(DailyTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    /**
     * Create a new task for today
     */
    public DailyTask createTask(String text) {
        return createTaskForDate(text, LocalDate.now());
    }
    
    /**
     * Create a new task for a specific date
     */
    public DailyTask createTaskForDate(String text, LocalDate date) {
        DailyTask task = new DailyTask(text, date);
        return taskRepository.save(task);
    }
    
    /**
     * Update an existing task's text
     */
    public DailyTask updateTask(Long taskId, String newText) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        Optional<DailyTask> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            DailyTask task = taskOpt.get();
            task.setText(newText);
            return taskRepository.save(task);
        }
        throw new IllegalArgumentException("Task not found with ID: " + taskId);
    }
    
    /**
     * Toggle task completion status
     */
    public DailyTask toggleTaskCompletion(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        Optional<DailyTask> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            DailyTask task = taskOpt.get();
            task.setCompleted(!task.isCompleted());
            return taskRepository.save(task);
        }
        throw new IllegalArgumentException("Task not found with ID: " + taskId);
    }
    
    /**
     * Mark task as completed
     */
    public DailyTask completeTask(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        Optional<DailyTask> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            DailyTask task = taskOpt.get();
            task.setCompleted(true);
            return taskRepository.save(task);
        }
        throw new IllegalArgumentException("Task not found with ID: " + taskId);
    }
    
    /**
     * Set task priority
     */
    public DailyTask setTaskPriority(Long taskId, int priority) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }
        Optional<DailyTask> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            DailyTask task = taskOpt.get();
            task.setPriority(priority);
            return taskRepository.save(task);
        }
        throw new IllegalArgumentException("Task not found with ID: " + taskId);
    }
    
    /**
     * Delete a task
     */
    public void deleteTask(Long taskId) {
        if (taskId != null && taskRepository.existsById(taskId)) {
            taskRepository.deleteById(taskId);
        } else {
            throw new IllegalArgumentException("Task not found with ID: " + taskId);
        }
    }
    
    /**
     * Get all tasks for today
     */
    public List<DailyTask> getTodaysTasks() {
        return getTasksForDate(LocalDate.now());
    }
    
    /**
     * Get all tasks for a specific date
     */
    public List<DailyTask> getTasksForDate(LocalDate date) {
        return taskRepository.findTasksForDate(date);
    }
    
    /**
     * Get incomplete tasks for today
     */
    public List<DailyTask> getIncompleteTasksForToday() {
        return taskRepository.findIncompleteTasksForDate(LocalDate.now());
    }
    
    /**
     * Get completed tasks for today
     */
    public List<DailyTask> getCompletedTasksForToday() {
        return taskRepository.findCompletedTasksForDate(LocalDate.now());
    }
    
    /**
     * Get overdue tasks (incomplete tasks from previous days)
     */
    public List<DailyTask> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDate.now());
    }
    
    /**
     * Get task completion statistics for a date
     */
    public TaskStats getTaskStatsForDate(LocalDate date) {
        long totalTasks = taskRepository.countTasksForDate(date);
        long completedTasks = taskRepository.countCompletedTasksForDate(date);
        
        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;
        
        return new TaskStats(date, (int) totalTasks, (int) completedTasks, completionRate);
    }
    
    /**
     * Get task statistics for today
     */
    public TaskStats getTodaysTaskStats() {
        return getTaskStatsForDate(LocalDate.now());
    }
    
    /**
     * Clean up old tasks (older than specified days)
     */
    public void cleanupOldTasks(int daysToKeep) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysToKeep);
        taskRepository.deleteByTaskDateBefore(cutoffDate);
    }
    
    /**
     * Get a specific task by ID
     */
    public Optional<DailyTask> getTask(Long taskId) {
        if (taskId == null) {
            return Optional.empty();
        }
        return taskRepository.findById(taskId);
    }
    
    /**
     * Clear all tasks for today (reset functionality)
     */
    public void clearTodaysTasks() {
        List<DailyTask> todaysTasks = getTodaysTasks();
        if (todaysTasks != null && !todaysTasks.isEmpty()) {
            taskRepository.deleteAll(todaysTasks);
        }
    }
    
    /**
     * Data class for task statistics
     */
    public static class TaskStats {
        private LocalDate date;
        private int totalTasks;
        private int completedTasks;
        private double completionRate;
        
        public TaskStats(LocalDate date, int totalTasks, int completedTasks, double completionRate) {
            this.date = date;
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.completionRate = completionRate;
        }
        
        // Getters
        public LocalDate getDate() { return date; }
        public int getTotalTasks() { return totalTasks; }
        public int getCompletedTasks() { return completedTasks; }
        public int getIncompleteTasks() { return totalTasks - completedTasks; }
        public double getCompletionRate() { return completionRate; }
        
        // Setters
        public void setDate(LocalDate date) { this.date = date; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    }
}