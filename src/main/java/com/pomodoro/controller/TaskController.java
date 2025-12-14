package com.pomodoro.controller;

import com.pomodoro.model.DailyTask;
import com.pomodoro.service.TaskManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for daily task management
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskManagementService taskManagementService;

    @Autowired
    public TaskController(TaskManagementService taskManagementService) {
        this.taskManagementService = taskManagementService;
    }

    /**
     * Get all tasks for today
     */
    @GetMapping
    public ResponseEntity<List<DailyTask>> getTodaysTasks() {
        return ResponseEntity.ok(taskManagementService.getTodaysTasks());
    }

    /**
     * Get tasks for a specific date
     */
    @GetMapping("/{date}")
    public ResponseEntity<List<DailyTask>> getTasksForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(taskManagementService.getTasksForDate(date));
    }

    /**
     * Get incomplete tasks for today
     */
    @GetMapping("/incomplete")
    public ResponseEntity<List<DailyTask>> getIncompleteTasksForToday() {
        return ResponseEntity.ok(taskManagementService.getIncompleteTasksForToday());
    }

    /**
     * Get completed tasks for today
     */
    @GetMapping("/completed")
    public ResponseEntity<List<DailyTask>> getCompletedTasksForToday() {
        return ResponseEntity.ok(taskManagementService.getCompletedTasksForToday());
    }

    /**
     * Get overdue tasks
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<DailyTask>> getOverdueTasks() {
        return ResponseEntity.ok(taskManagementService.getOverdueTasks());
    }

    /**
     * Create a new task for today
     */
    @PostMapping
    public ResponseEntity<DailyTask> createTask(@RequestBody CreateTaskRequest request) {
        DailyTask task = taskManagementService.createTask(request.getText());
        if (request.getPriority() != null) {
            task = taskManagementService.setTaskPriority(task.getId(), request.getPriority());
        }
        return ResponseEntity.ok(task);
    }

    /**
     * Create a task for a specific date
     */
    @PostMapping("/{date}")
    public ResponseEntity<DailyTask> createTaskForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody CreateTaskRequest request) {
        DailyTask task = taskManagementService.createTaskForDate(request.getText(), date);
        if (request.getPriority() != null) {
            task = taskManagementService.setTaskPriority(task.getId(), request.getPriority());
        }
        return ResponseEntity.ok(task);
    }

    /**
     * Update a task's text
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<DailyTask> updateTask(
            @PathVariable Long taskId, 
            @RequestBody UpdateTaskRequest request) {
        DailyTask task = taskManagementService.updateTask(taskId, request.getText());
        return ResponseEntity.ok(task);
    }

    /**
     * Toggle task completion
     */
    @PatchMapping("/{taskId}/toggle")
    public ResponseEntity<DailyTask> toggleTaskCompletion(@PathVariable Long taskId) {
        DailyTask task = taskManagementService.toggleTaskCompletion(taskId);
        return ResponseEntity.ok(task);
    }

    /**
     * Mark task as completed
     */
    @PatchMapping("/{taskId}/complete")
    public ResponseEntity<DailyTask> completeTask(@PathVariable Long taskId) {
        DailyTask task = taskManagementService.completeTask(taskId);
        return ResponseEntity.ok(task);
    }

    /**
     * Set task priority
     */
    @PatchMapping("/{taskId}/priority")
    public ResponseEntity<DailyTask> setTaskPriority(
            @PathVariable Long taskId, 
            @RequestBody SetPriorityRequest request) {
        DailyTask task = taskManagementService.setTaskPriority(taskId, request.getPriority());
        return ResponseEntity.ok(task);
    }

    /**
     * Get a specific task
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<DailyTask> getTask(@PathVariable Long taskId) {
        Optional<DailyTask> task = taskManagementService.getTask(taskId);
        return task.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a task
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskManagementService.deleteTask(taskId);
        return ResponseEntity.ok().build();
    }

    /**
     * Clear all tasks for today
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearTodaysTasks() {
        taskManagementService.clearTodaysTasks();
        return ResponseEntity.ok().build();
    }

    /**
     * Get task statistics for today
     */
    @GetMapping("/stats")
    public ResponseEntity<TaskManagementService.TaskStats> getTodaysTaskStats() {
        return ResponseEntity.ok(taskManagementService.getTodaysTaskStats());
    }

    /**
     * Get task statistics for a specific date
     */
    @GetMapping("/stats/{date}")
    public ResponseEntity<TaskManagementService.TaskStats> getTaskStatsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(taskManagementService.getTaskStatsForDate(date));
    }

    /**
     * Request DTO for creating tasks
     */
    public static class CreateTaskRequest {
        private String text;
        private Integer priority = 0;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
    }

    /**
     * Request DTO for updating task text
     */
    public static class UpdateTaskRequest {
        private String text;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    /**
     * Request DTO for setting task priority
     */
    public static class SetPriorityRequest {
        private int priority;

        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
    }
}