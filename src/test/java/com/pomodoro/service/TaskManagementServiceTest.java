package com.pomodoro.service;

import com.pomodoro.model.*;
import com.pomodoro.repository.DailyTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for TaskManagementService
 * Covers the testing plan from product-reference:
 * - Tasks are properly managed with CRUD operations
 * - Daily task reset functionality
 * - Task completion tracking
 * - Data persistence
 */
class TaskManagementServiceTest {

    private TaskManagementService taskManagementService;
    
    @Mock
    private DailyTaskRepository mockRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taskManagementService = new TaskManagementService(mockRepository);
    }

    @Test
    @DisplayName("Should create new task successfully")
    void testCreateTask() {
        // Given - New task data
        String taskText = "Complete project documentation";
        LocalDate today = LocalDate.now();
        
        DailyTask savedTask = new DailyTask(taskText, today);
        savedTask.setId(1L);
        
        when(mockRepository.save(any())).thenReturn(savedTask);
        
        // When - Create task
        DailyTask result = taskManagementService.createTask(taskText);
        
        // Then - Task should be created correctly
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(taskText, result.getText());
        assertEquals(today, result.getTaskDate());
        assertFalse(result.isCompleted());
        assertEquals(0, result.getPriority());
        
        verify(mockRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should create task for specific date")
    void testCreateTaskForDate() {
        // Given - New task data for specific date
        String taskText = "Historical task";
        LocalDate specificDate = LocalDate.of(2025, 12, 10);
        
        DailyTask savedTask = new DailyTask(taskText, specificDate);
        savedTask.setId(1L);
        
        when(mockRepository.save(any())).thenReturn(savedTask);
        
        // When - Create task for specific date
        DailyTask result = taskManagementService.createTaskForDate(taskText, specificDate);
        
        // Then - Task should be created with specific date
        assertNotNull(result);
        assertEquals(taskText, result.getText());
        assertEquals(specificDate, result.getTaskDate());
        
        verify(mockRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should get all tasks for today")
    void testGetTodaysTasks() {
        // Given - Mock tasks for today
        LocalDate today = LocalDate.now();
        List<DailyTask> mockTasks = Arrays.asList(
            new DailyTask("Task 1", today),
            new DailyTask("Task 2", today)
        );
        mockTasks.get(0).setId(1L);
        mockTasks.get(1).setId(2L);
        
        when(mockRepository.findTasksForDate(today)).thenReturn(mockTasks);
        
        // When - Get today's tasks
        List<DailyTask> result = taskManagementService.getTodaysTasks();
        
        // Then - Should return today's tasks
        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).getText());
        assertEquals("Task 2", result.get(1).getText());
        assertTrue(result.stream().allMatch(task -> task.getTaskDate().equals(today)));
        
        verify(mockRepository).findTasksForDate(today);
    }

    @Test
    @DisplayName("Should get tasks for specific date")
    void testGetTasksForSpecificDate() {
        // Given - Mock tasks for specific date
        LocalDate specificDate = LocalDate.of(2025, 12, 10);
        List<DailyTask> mockTasks = Arrays.asList(
            new DailyTask("Historical Task 1", specificDate),
            new DailyTask("Historical Task 2", specificDate)
        );
        
        when(mockRepository.findTasksForDate(specificDate)).thenReturn(mockTasks);
        
        // When - Get tasks for specific date
        List<DailyTask> result = taskManagementService.getTasksForDate(specificDate);
        
        // Then - Should return tasks for that date
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(task -> task.getTaskDate().equals(specificDate)));
        
        verify(mockRepository).findTasksForDate(specificDate);
    }

    @Test
    @DisplayName("Should complete task successfully")
    void testCompleteTask() {
        // Given - Existing task
        Long taskId = 1L;
        DailyTask existingTask = new DailyTask("Test Task", LocalDate.now());
        existingTask.setId(taskId);
        existingTask.setCompleted(false);
        
        DailyTask completedTask = new DailyTask("Test Task", LocalDate.now());
        completedTask.setId(taskId);
        completedTask.setCompleted(true);
        
        when(mockRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(mockRepository.save(any())).thenReturn(completedTask);
        
        // When - Complete task
        DailyTask result = taskManagementService.completeTask(taskId);
        
        // Then - Task should be marked as completed
        assertNotNull(result);
        assertTrue(result.isCompleted());
        
        verify(mockRepository).findById(taskId);
        verify(mockRepository).save(any());
    }

    @Test
    @DisplayName("Should handle task not found when completing")
    void testCompleteTaskNotFound() {
        // Given - Non-existent task ID
        Long taskId = 999L;
        when(mockRepository.findById(taskId)).thenReturn(Optional.empty());
        
        // When/Then - Should throw exception for non-existent task
        assertThrows(IllegalArgumentException.class, () -> {
            taskManagementService.completeTask(taskId);
        });
        
        verify(mockRepository).findById(taskId);
        verify(mockRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should toggle task completion successfully")
    void testToggleTaskCompletion() {
        // Given - Existing incomplete task
        Long taskId = 1L;
        DailyTask existingTask = new DailyTask("Test Task", LocalDate.now());
        existingTask.setId(taskId);
        existingTask.setCompleted(false);
        
        DailyTask toggledTask = new DailyTask("Test Task", LocalDate.now());
        toggledTask.setId(taskId);
        toggledTask.setCompleted(true);
        
        when(mockRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(mockRepository.save(any())).thenReturn(toggledTask);
        
        // When - Toggle task completion
        DailyTask result = taskManagementService.toggleTaskCompletion(taskId);
        
        // Then - Task completion should be toggled
        assertNotNull(result);
        assertTrue(result.isCompleted());
        
        verify(mockRepository).findById(taskId);
        verify(mockRepository).save(any());
    }

    @Test
    @DisplayName("Should set task priority successfully")
    void testSetTaskPriority() {
        // Given - Task with initial priority
        Long taskId = 1L;
        DailyTask task = new DailyTask("Test Task", LocalDate.now());
        task.setId(taskId);
        task.setPriority(0);
        
        DailyTask updatedTask = new DailyTask("Test Task", LocalDate.now());
        updatedTask.setId(taskId);
        updatedTask.setPriority(1);
        
        when(mockRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(mockRepository.save(any())).thenReturn(updatedTask);
        
        // When - Set task priority
        DailyTask result = taskManagementService.setTaskPriority(taskId, 1);
        
        // Then - Priority should be updated
        assertNotNull(result);
        assertEquals(1, result.getPriority());
        
        verify(mockRepository).findById(taskId);
        verify(mockRepository).save(any());
    }

    @Test
    @DisplayName("Should update task text successfully")
    void testUpdateTask() {
        // Given - Existing task and new text
        Long taskId = 1L;
        String newText = "Updated task text";
        
        DailyTask existingTask = new DailyTask("Old Text", LocalDate.now());
        existingTask.setId(taskId);
        
        DailyTask updatedTask = new DailyTask(newText, LocalDate.now());
        updatedTask.setId(taskId);
        
        when(mockRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(mockRepository.save(any())).thenReturn(updatedTask);
        
        // When - Update task
        DailyTask result = taskManagementService.updateTask(taskId, newText);
        
        // Then - Task text should be updated
        assertNotNull(result);
        assertEquals(newText, result.getText());
        
        verify(mockRepository).findById(taskId);
        verify(mockRepository).save(any());
    }

    @Test
    @DisplayName("Should delete task successfully")
    void testDeleteTask() {
        // Given - Task ID to delete
        Long taskId = 1L;
        when(mockRepository.existsById(taskId)).thenReturn(true);
        
        // When - Delete task
        taskManagementService.deleteTask(taskId);
        
        // Then - Repository delete should be called
        verify(mockRepository).existsById(taskId);
        verify(mockRepository).deleteById(taskId);
    }

    @Test
    @DisplayName("Should handle delete task not found")
    void testDeleteTaskNotFound() {
        // Given - Non-existent task ID
        Long taskId = 999L;
        when(mockRepository.existsById(taskId)).thenReturn(false);
        
        // When/Then - Should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            taskManagementService.deleteTask(taskId);
        });
        
        verify(mockRepository).existsById(taskId);
        verify(mockRepository, never()).deleteById(taskId);
    }

    @Test
    @DisplayName("Should get incomplete tasks for today")
    void testGetIncompleteTasksForToday() {
        // Given - Mock incomplete tasks
        LocalDate today = LocalDate.now();
        List<DailyTask> mockTasks = Arrays.asList(
            new DailyTask("Incomplete Task 1", today),
            new DailyTask("Incomplete Task 2", today)
        );
        
        when(mockRepository.findIncompleteTasksForDate(today)).thenReturn(mockTasks);
        
        // When - Get incomplete tasks
        List<DailyTask> result = taskManagementService.getIncompleteTasksForToday();
        
        // Then - Should return incomplete tasks
        assertEquals(2, result.size());
        
        verify(mockRepository).findIncompleteTasksForDate(today);
    }

    @Test
    @DisplayName("Should get completed tasks for today")
    void testGetCompletedTasksForToday() {
        // Given - Mock completed tasks
        LocalDate today = LocalDate.now();
        List<DailyTask> mockTasks = Arrays.asList(
            new DailyTask("Completed Task 1", today),
            new DailyTask("Completed Task 2", today)
        );
        mockTasks.forEach(task -> task.setCompleted(true));
        
        when(mockRepository.findCompletedTasksForDate(today)).thenReturn(mockTasks);
        
        // When - Get completed tasks
        List<DailyTask> result = taskManagementService.getCompletedTasksForToday();
        
        // Then - Should return completed tasks
        assertEquals(2, result.size());
        
        verify(mockRepository).findCompletedTasksForDate(today);
    }

    @Test
    @DisplayName("Should get overdue tasks")
    void testGetOverdueTasks() {
        // Given - Mock overdue tasks
        LocalDate today = LocalDate.now();
        List<DailyTask> mockTasks = Arrays.asList(
            new DailyTask("Overdue Task 1", today.minusDays(1)),
            new DailyTask("Overdue Task 2", today.minusDays(2))
        );
        
        when(mockRepository.findOverdueTasks(today)).thenReturn(mockTasks);
        
        // When - Get overdue tasks
        List<DailyTask> result = taskManagementService.getOverdueTasks();
        
        // Then - Should return overdue tasks
        assertEquals(2, result.size());
        
        verify(mockRepository).findOverdueTasks(today);
    }

    @Test
    @DisplayName("Should handle empty task list gracefully")
    void testGetTasksForEmptyDate() {
        // Given - Date with no tasks
        LocalDate emptyDate = LocalDate.of(2025, 1, 1);
        when(mockRepository.findTasksForDate(emptyDate)).thenReturn(Arrays.asList());
        
        // When - Get tasks for empty date
        List<DailyTask> result = taskManagementService.getTasksForDate(emptyDate);
        
        // Then - Should return empty list
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(mockRepository).findTasksForDate(emptyDate);
    }

    @Test
    @DisplayName("Should validate task data correctly")
    void testTaskDataValidation() {
        // Test null task ID handling
        assertThrows(IllegalArgumentException.class, () -> {
            taskManagementService.completeTask(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            taskManagementService.updateTask(null, "Test");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            taskManagementService.toggleTaskCompletion(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            taskManagementService.setTaskPriority(null, 1);
        });
    }

    @Test
    @DisplayName("Should validate task not found scenarios")
    void testTaskNotFoundValidation() {
        // Given - Non-existent task ID
        Long taskId = 999L;
        when(mockRepository.findById(taskId)).thenReturn(Optional.empty());
        
        // When/Then - Should throw exceptions for various operations
        assertThrows(IllegalArgumentException.class, () -> {
            taskManagementService.updateTask(taskId, "New text");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            taskManagementService.toggleTaskCompletion(taskId);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            taskManagementService.setTaskPriority(taskId, 1);
        });
    }
}