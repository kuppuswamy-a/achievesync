package com.achievesync.goalservice.projection;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "goal_streaks")
public class GoalStreakProjection {
    
    @Id
    @Column(name = "goal_id")
    private String goalId;
    
    @Column(name = "current_streak_days")
    private int currentStreakDays;
    
    @Column(name = "longest_streak_days")
    private int longestStreakDays;
    
    @Column(name = "last_streak_update")
    private LocalDate lastStreakUpdate;
    
    @Column(name = "is_streak_active")
    private boolean isStreakActive;

    public GoalStreakProjection() {}
    
    public GoalStreakProjection(String goalId, int currentStreakDays, int longestStreakDays, 
                               LocalDate lastStreakUpdate, boolean isStreakActive) {
        this.goalId = goalId;
        this.currentStreakDays = currentStreakDays;
        this.longestStreakDays = longestStreakDays;
        this.lastStreakUpdate = lastStreakUpdate;
        this.isStreakActive = isStreakActive;
    }

    // Getters and Setters
    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }
    
    public int getCurrentStreakDays() { return currentStreakDays; }
    public void setCurrentStreakDays(int currentStreakDays) { this.currentStreakDays = currentStreakDays; }
    
    public int getLongestStreakDays() { return longestStreakDays; }
    public void setLongestStreakDays(int longestStreakDays) { this.longestStreakDays = longestStreakDays; }
    
    public LocalDate getLastStreakUpdate() { return lastStreakUpdate; }
    public void setLastStreakUpdate(LocalDate lastStreakUpdate) { this.lastStreakUpdate = lastStreakUpdate; }
    
    public boolean isStreakActive() { return isStreakActive; }
    public void setStreakActive(boolean streakActive) { isStreakActive = streakActive; }
}