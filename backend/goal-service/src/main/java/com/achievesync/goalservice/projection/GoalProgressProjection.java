package com.achievesync.goalservice.projection;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "goal_progress")
public class GoalProgressProjection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "progress_id")
    private String progressId;
    
    @Column(name = "goal_id", nullable = false)
    private String goalId;
    
    @Column(name = "progress_percentage", nullable = false)
    private double progressPercentage;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "update_timestamp")
    private Instant updateTimestamp;

    public GoalProgressProjection() {}

    public GoalProgressProjection(String goalId, double progressPercentage, String notes, Instant updateTimestamp) {
        this.goalId = goalId;
        this.progressPercentage = progressPercentage;
        this.notes = notes;
        this.updateTimestamp = updateTimestamp;
    }

    public GoalProgressProjection(String progressId, String goalId, double progressPercentage, String notes, Instant updateTimestamp) {
        this.progressId = progressId;
        this.goalId = goalId;
        this.progressPercentage = progressPercentage;
        this.notes = notes;
        this.updateTimestamp = updateTimestamp;
    }

    // Getters and Setters
    public String getProgressId() { return progressId; }
    public void setProgressId(String progressId) { this.progressId = progressId; }
    
    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }
    
    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Instant getUpdateTimestamp() { return updateTimestamp; }
    public void setUpdateTimestamp(Instant updateTimestamp) { this.updateTimestamp = updateTimestamp; }
}