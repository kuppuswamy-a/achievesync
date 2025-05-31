package com.achievesync.goalservice.service;

import com.achievesync.goalservice.projection.GoalProgressProjection;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class StreakCalculationService {

    public StreakData calculateStreak(List<GoalProgressProjection> progressHistory) {
        if (progressHistory == null || progressHistory.isEmpty()) {
            return new StreakData(0, 0, null, false);
        }

        // Sort by timestamp descending (most recent first)
        progressHistory.sort((a, b) -> b.getUpdateTimestamp().compareTo(a.getUpdateTimestamp()));

        int currentStreak = 0;
        int longestStreak = 0;
        LocalDate lastProgressDate = null;
        boolean isStreakActive = false;

        LocalDate previousDate = null;
        int tempStreak = 0;

        for (GoalProgressProjection progress : progressHistory) {
            LocalDate progressDate = progress.getUpdateTimestamp().atZone(ZoneId.systemDefault()).toLocalDate();
            
            if (lastProgressDate == null) {
                lastProgressDate = progressDate;
            }

            if (previousDate == null) {
                tempStreak = 1;
                previousDate = progressDate;
                continue;
            }

            long daysBetween = ChronoUnit.DAYS.between(progressDate, previousDate);
            
            if (daysBetween == 1) {
                // Consecutive days
                tempStreak++;
            } else {
                // Break in streak
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak;
                }
                
                if (currentStreak == 0 && previousDate.equals(lastProgressDate)) {
                    currentStreak = tempStreak;
                }
                
                tempStreak = 1;
            }
            
            previousDate = progressDate;
        }

        // Handle the last streak
        if (tempStreak > longestStreak) {
            longestStreak = tempStreak;
        }
        
        if (currentStreak == 0) {
            currentStreak = tempStreak;
        }

        // Check if streak is still active (last progress within 2 days)
        if (lastProgressDate != null) {
            long daysSinceLastProgress = ChronoUnit.DAYS.between(lastProgressDate, LocalDate.now());
            isStreakActive = daysSinceLastProgress <= 1;
            
            if (!isStreakActive) {
                currentStreak = 0;
            }
        }

        return new StreakData(currentStreak, longestStreak, lastProgressDate, isStreakActive);
    }

    public static class StreakData {
        private final int currentStreakDays;
        private final int longestStreakDays;
        private final LocalDate lastStreakUpdate;
        private final boolean isStreakActive;

        public StreakData(int currentStreakDays, int longestStreakDays, LocalDate lastStreakUpdate, boolean isStreakActive) {
            this.currentStreakDays = currentStreakDays;
            this.longestStreakDays = longestStreakDays;
            this.lastStreakUpdate = lastStreakUpdate;
            this.isStreakActive = isStreakActive;
        }

        public int getCurrentStreakDays() { return currentStreakDays; }
        public int getLongestStreakDays() { return longestStreakDays; }
        public LocalDate getLastStreakUpdate() { return lastStreakUpdate; }
        public boolean isStreakActive() { return isStreakActive; }
    }
}