package com.pomodoro.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a daily task
 * Tasks are automatically scoped to a specific date and reset each day
 */
@Entity
@Table(name = "daily_tasks")
public class DailyTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String text;
    
    @Column(nullable = false)
    private boolean completed = false;
    
    @Column(nullable = false)
    private LocalDate taskDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime completedAt;
    
    @Column(nullable = false)
    private int priority = 0; // 0 = normal, 1 = high priority
    
    public DailyTask() {
        this.createdAt = LocalDateTime.now();
        this.taskDate = LocalDate.now();
    }
    
    public DailyTask(String text) {
        this();
        this.text = text;
    }
    
    public DailyTask(String text, LocalDate taskDate) {
        this();
        this.text = text;
        this.taskDate = taskDate;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && completedAt == null) {
            this.completedAt = LocalDateTime.now();
        } else if (!completed) {
            this.completedAt = null;
        }
    }
    
    public LocalDate getTaskDate() {
        return taskDate;
    }
    
    public void setTaskDate(LocalDate taskDate) {
        this.taskDate = taskDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    // Helper methods
    public boolean isOverdue() {
        return !completed && taskDate.isBefore(LocalDate.now());
    }
    
    public boolean isDueToday() {
        return taskDate.equals(LocalDate.now());
    }
    
    public boolean isHighPriority() {
        return priority > 0;
    }
}