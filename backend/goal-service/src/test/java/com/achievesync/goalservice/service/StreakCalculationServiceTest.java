package com.achievesync.goalservice.service;

import com.achievesync.goalservice.projection.GoalProgressProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StreakCalculationServiceTest {

    private StreakCalculationService streakCalculationService;

    @BeforeEach
    void setUp() {
        streakCalculationService = new StreakCalculationService();
    }

    @Test
    void testCalculateStreak_EmptyList() {
        List<GoalProgressProjection> progressHistory = new ArrayList<>();
        
        StreakCalculationService.StreakData result = streakCalculationService.calculateStreak(progressHistory);
        
        assertEquals(0, result.getCurrentStreakDays());
        assertEquals(0, result.getLongestStreakDays());
        assertNull(result.getLastStreakUpdate());
        assertFalse(result.isStreakActive());
    }

    @Test
    void testCalculateStreak_NullList() {
        StreakCalculationService.StreakData result = streakCalculationService.calculateStreak(null);
        
        assertEquals(0, result.getCurrentStreakDays());
        assertEquals(0, result.getLongestStreakDays());
        assertNull(result.getLastStreakUpdate());
        assertFalse(result.isStreakActive());
    }

    @Test
    void testCalculateStreak_SingleEntry() {
        List<GoalProgressProjection> progressHistory = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        progressHistory.add(createProgressEntry("goal1", today, 25.0));
        
        StreakCalculationService.StreakData result = streakCalculationService.calculateStreak(progressHistory);
        
        assertEquals(1, result.getCurrentStreakDays());
        assertEquals(1, result.getLongestStreakDays());
        assertEquals(today, result.getLastStreakUpdate());
        assertTrue(result.isStreakActive());
    }

    @Test
    void testCalculateStreak_ConsecutiveDays() {
        List<GoalProgressProjection> progressHistory = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // Add 5 consecutive days of progress (most recent first)
        for (int i = 0; i < 5; i++) {
            progressHistory.add(createProgressEntry("goal1", today.minusDays(i), 20.0 * (i + 1)));
        }
        
        StreakCalculationService.StreakData result = streakCalculationService.calculateStreak(progressHistory);
        
        assertEquals(5, result.getCurrentStreakDays());
        assertEquals(5, result.getLongestStreakDays());
        assertEquals(today, result.getLastStreakUpdate());
        assertTrue(result.isStreakActive());
    }

    @Test
    void testCalculateStreak_BrokenStreak() {
        List<GoalProgressProjection> progressHistory = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // Current streak of 2 days
        progressHistory.add(createProgressEntry("goal1", today, 60.0));
        progressHistory.add(createProgressEntry("goal1", today.minusDays(1), 50.0));
        
        // Gap of 2 days
        
        // Previous streak of 3 days
        progressHistory.add(createProgressEntry("goal1", today.minusDays(4), 40.0));
        progressHistory.add(createProgressEntry("goal1", today.minusDays(5), 30.0));
        progressHistory.add(createProgressEntry("goal1", today.minusDays(6), 20.0));
        
        StreakCalculationService.StreakData result = streakCalculationService.calculateStreak(progressHistory);
        
        assertEquals(2, result.getCurrentStreakDays());
        assertEquals(3, result.getLongestStreakDays());
        assertEquals(today, result.getLastStreakUpdate());
        assertTrue(result.isStreakActive());
    }

    @Test
    void testCalculateStreak_InactiveStreak() {
        List<GoalProgressProjection> progressHistory = new ArrayList<>();
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        
        // Add progress from 3 days ago (streak should be inactive)
        progressHistory.add(createProgressEntry("goal1", threeDaysAgo, 50.0));
        progressHistory.add(createProgressEntry("goal1", threeDaysAgo.minusDays(1), 40.0));
        
        StreakCalculationService.StreakData result = streakCalculationService.calculateStreak(progressHistory);
        
        assertEquals(0, result.getCurrentStreakDays()); // Should be 0 due to inactivity
        assertEquals(2, result.getLongestStreakDays());
        assertEquals(threeDaysAgo, result.getLastStreakUpdate());
        assertFalse(result.isStreakActive());
    }

    @Test
    void testCalculateStreak_MultipleBreaks() {
        List<GoalProgressProjection> progressHistory = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        // Current streak: 1 day
        progressHistory.add(createProgressEntry("goal1", today, 90.0));
        
        // Gap
        
        // Previous streak: 2 days
        progressHistory.add(createProgressEntry("goal1", today.minusDays(3), 70.0));
        progressHistory.add(createProgressEntry("goal1", today.minusDays(4), 60.0));
        
        // Gap
        
        // Longest streak: 4 days
        progressHistory.add(createProgressEntry("goal1", today.minusDays(7), 50.0));
        progressHistory.add(createProgressEntry("goal1", today.minusDays(8), 40.0));
        progressHistory.add(createProgressEntry("goal1", today.minusDays(9), 30.0));
        progressHistory.add(createProgressEntry("goal1", today.minusDays(10), 20.0));
        
        StreakCalculationService.StreakData result = streakCalculationService.calculateStreak(progressHistory);
        
        assertEquals(1, result.getCurrentStreakDays());
        assertEquals(4, result.getLongestStreakDays());
        assertEquals(today, result.getLastStreakUpdate());
        assertTrue(result.isStreakActive());
    }

    private GoalProgressProjection createProgressEntry(String goalId, LocalDate date, double progress) {
        return new GoalProgressProjection(
            goalId + "_progress_" + date.toString(),
            goalId,
            progress,
            "Test progress",
            date.atStartOfDay().toInstant(ZoneOffset.UTC)
        );
    }
}