package com.pomodoro.service;

import com.pomodoro.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for ProgressChartService
 * Covers the testing plan from product-reference:
 * - Progress chart displays correctly with 4-color productivity levels
 * - Progress data calculation is accurate
 * - GitHub-style visual representation
 */
class ProgressChartServiceTest {

    private ProgressChartService progressChartService;
    
    @Mock
    private SessionLoggingService mockSessionLoggingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        progressChartService = new ProgressChartService(mockSessionLoggingService);
    }

    @Test
    @DisplayName("Should generate progress chart for specific year")
    void testGenerateProgressChartForYear() {
        // Given - Mock productivity data for 2025
        when(mockSessionLoggingService.getProductivityLevelForDate(any()))
            .thenReturn(2); // Medium productivity
        when(mockSessionLoggingService.getTotalWorkHoursForDate(any()))
            .thenReturn(2.5);
        when(mockSessionLoggingService.getWorkSessionCountForDate(any()))
            .thenReturn(5);
        
        // When - Generate progress chart for 2025
        ProgressChart result = progressChartService.generateProgressChart(2025);
        
        // Then - Should have correct year data
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(12, result.getMonths().size()); // 12 months
        
        // Verify each month has correct structure
        for (ProgressMonth month : result.getMonths()) {
            assertEquals(2025, month.getYear());
            assertNotNull(month.getMonth());
            assertNotNull(month.getDays());
            assertTrue(month.getDays().size() >= 28); // At least 28 days
        }
    }

    @Test
    @DisplayName("Should generate progress month for December with correct days")
    void testGenerateProgressMonth() {
        // Given - Mock productivity data for December 2025
        when(mockSessionLoggingService.getProductivityLevelForDate(any()))
            .thenReturn(3); // High productivity
        when(mockSessionLoggingService.getTotalWorkHoursForDate(any()))
            .thenReturn(4.5);
        when(mockSessionLoggingService.getWorkSessionCountForDate(any()))
            .thenReturn(9);
        
        // When - Generate progress month for December 2025
        ProgressMonth result = progressChartService.generateProgressMonth(2025, Month.DECEMBER);
        
        // Then - Should have correct month data
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(Month.DECEMBER, result.getMonth());
        assertEquals(31, result.getDays().size()); // December has 31 days
        
        // Verify each day has correct data
        for (ProgressDay day : result.getDays()) {
            assertEquals(3, day.getProductivityLevel());
            assertEquals(4.5, day.getTotalHours(), 0.01);
            assertEquals(9, day.getSessionCount());
        }
    }

    @Test
    @DisplayName("Should generate progress day with correct data")
    void testGenerateProgressDay() {
        // Given - Mock session data for specific date
        LocalDate testDate = LocalDate.of(2025, 12, 14);
        when(mockSessionLoggingService.getTotalWorkHoursForDate(testDate))
            .thenReturn(3.5);
        when(mockSessionLoggingService.getWorkSessionCountForDate(testDate))
            .thenReturn(7);
        when(mockSessionLoggingService.getProductivityLevelForDate(testDate))
            .thenReturn(2);
        
        // When - Generate progress day
        ProgressDay result = progressChartService.generateProgressDay(testDate);
        
        // Then - Should have correct day data
        assertNotNull(result);
        assertEquals(testDate, result.getDate());
        assertEquals(3.5, result.getTotalHours(), 0.01);
        assertEquals(7, result.getSessionCount());
        assertEquals(2, result.getProductivityLevel());
    }

    @Test
    @DisplayName("Should calculate correct productivity levels for GitHub-style chart")
    void testProductivityLevels() {
        // Given - Mock different productivity scenarios
        LocalDate date1 = LocalDate.of(2025, 12, 1);
        LocalDate date2 = LocalDate.of(2025, 12, 2);
        LocalDate date3 = LocalDate.of(2025, 12, 3);
        LocalDate date4 = LocalDate.of(2025, 12, 4);
        
        when(mockSessionLoggingService.getProductivityLevelForDate(date1)).thenReturn(0); // No work
        when(mockSessionLoggingService.getProductivityLevelForDate(date2)).thenReturn(1); // Low work
        when(mockSessionLoggingService.getProductivityLevelForDate(date3)).thenReturn(2); // Medium work
        when(mockSessionLoggingService.getProductivityLevelForDate(date4)).thenReturn(3); // High work
        
        when(mockSessionLoggingService.getTotalWorkHoursForDate(any())).thenReturn(1.0);
        when(mockSessionLoggingService.getWorkSessionCountForDate(any())).thenReturn(2);
        
        // When - Generate progress days
        ProgressDay day1 = progressChartService.generateProgressDay(date1);
        ProgressDay day2 = progressChartService.generateProgressDay(date2);
        ProgressDay day3 = progressChartService.generateProgressDay(date3);
        ProgressDay day4 = progressChartService.generateProgressDay(date4);
        
        // Then - Should have correct productivity levels
        assertEquals(0, day1.getProductivityLevel()); // No work (gray)
        assertEquals(1, day2.getProductivityLevel()); // Low work (light green)
        assertEquals(2, day3.getProductivityLevel()); // Medium work (medium green)
        assertEquals(3, day4.getProductivityLevel()); // High work (dark green)
    }

    @Test
    @DisplayName("Should get current year progress chart")
    void testGetCurrentYearProgressChart() {
        // Given - Current year data
        int currentYear = LocalDate.now().getYear();
        when(mockSessionLoggingService.getProductivityLevelForDate(any())).thenReturn(2);
        when(mockSessionLoggingService.getTotalWorkHoursForDate(any())).thenReturn(2.0);
        when(mockSessionLoggingService.getWorkSessionCountForDate(any())).thenReturn(4);
        
        // When - Get current year chart
        ProgressChart result = progressChartService.getCurrentYearProgressChart();
        
        // Then - Should have current year data
        assertNotNull(result);
        assertEquals(currentYear, result.getYear());
        assertEquals(12, result.getMonths().size());
    }

    @Test
    @DisplayName("Should get available years correctly")
    void testGetAvailableYears() {
        // When - Get available years
        List<Integer> result = progressChartService.getAvailableYears();
        
        // Then - Should include reasonable years
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(LocalDate.now().getYear()));
        // Should start from 2025 or current year - 2
        int minExpected = Math.max(2025, LocalDate.now().getYear() - 2);
        assertTrue(result.stream().anyMatch(year -> year >= minExpected));
    }

    @Test
    @DisplayName("Should calculate yearly statistics correctly")
    void testGetYearlyStats() {
        // Given - Mock chart with statistics
        when(mockSessionLoggingService.getProductivityLevelForDate(any())).thenReturn(2);
        when(mockSessionLoggingService.getTotalWorkHoursForDate(any())).thenReturn(3.0);
        when(mockSessionLoggingService.getWorkSessionCountForDate(any())).thenReturn(6);
        
        // When - Get yearly stats
        ProgressChartService.YearlyStats result = progressChartService.getYearlyStats(2025);
        
        // Then - Should have correct statistics
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertTrue(result.getTotalHours() > 0);
        assertTrue(result.getTotalSessions() > 0);
        assertTrue(result.getWorkDays() >= 0);
        assertTrue(result.getAverageHoursPerDay() >= 0);
        assertTrue(result.getCurrentStreak() >= 0);
        assertTrue(result.getLongestStreak() >= 0);
    }

    @Test
    @DisplayName("Should handle February in leap year correctly")
    void testLeapYearFebruary() {
        // Given - 2024 is a leap year
        when(mockSessionLoggingService.getProductivityLevelForDate(any())).thenReturn(1);
        when(mockSessionLoggingService.getTotalWorkHoursForDate(any())).thenReturn(1.0);
        when(mockSessionLoggingService.getWorkSessionCountForDate(any())).thenReturn(2);
        
        // When - Generate February 2024 month
        ProgressMonth result = progressChartService.generateProgressMonth(2024, Month.FEBRUARY);
        
        // Then - Should have 29 days (leap year)
        assertNotNull(result);
        assertEquals(29, result.getDays().size());
        assertEquals(2024, result.getYear());
        assertEquals(Month.FEBRUARY, result.getMonth());
    }

    @Test
    @DisplayName("Should handle February in non-leap year correctly")
    void testNonLeapYearFebruary() {
        // Given - 2025 is not a leap year
        when(mockSessionLoggingService.getProductivityLevelForDate(any())).thenReturn(1);
        when(mockSessionLoggingService.getTotalWorkHoursForDate(any())).thenReturn(1.0);
        when(mockSessionLoggingService.getWorkSessionCountForDate(any())).thenReturn(2);
        
        // When - Generate February 2025 month
        ProgressMonth result = progressChartService.generateProgressMonth(2025, Month.FEBRUARY);
        
        // Then - Should have 28 days (non-leap year)
        assertNotNull(result);
        assertEquals(28, result.getDays().size());
        assertEquals(2025, result.getYear());
        assertEquals(Month.FEBRUARY, result.getMonth());
    }

    @Test
    @DisplayName("Should handle empty productivity data gracefully")
    void testEmptyProductivityData() {
        // Given - No productivity data
        when(mockSessionLoggingService.getProductivityLevelForDate(any())).thenReturn(0);
        when(mockSessionLoggingService.getTotalWorkHoursForDate(any())).thenReturn(0.0);
        when(mockSessionLoggingService.getWorkSessionCountForDate(any())).thenReturn(0);
        
        // When - Generate chart
        ProgressChart result = progressChartService.generateProgressChart(2025);
        
        // Then - Should handle gracefully with no work data
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(12, result.getMonths().size());
        
        // All days should show no productivity
        for (ProgressMonth month : result.getMonths()) {
            assertTrue(month.getDays().stream()
                .allMatch(day -> day.getProductivityLevel() == 0 && 
                               day.getTotalHours() == 0.0 && 
                               day.getSessionCount() == 0));
        }
    }
}