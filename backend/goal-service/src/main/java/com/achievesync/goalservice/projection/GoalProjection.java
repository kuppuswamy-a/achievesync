package com.achievesync.goalservice.projection;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "goals")
public class GoalProjection {
    
    @Id
    @Column(name = "goal_id")
    private String goalId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "target_date")
    private LocalDate targetDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GoalStatus status;
    
    @Column(name = "progress_percentage")
    private double progressPercentage;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "category")
    private String category;
    
    @ElementCollection
    @CollectionTable(name = "goal_tags", joinColumns = @JoinColumn(name = "goal_id"))
    @Column(name = "tag")
    private java.util.List<String> tags = new java.util.ArrayList<>();

    public GoalProjection() {}

    // Getters and Setters
    public String getGoalId() { return goalId; }
    public void setGoalId(String goalId) { this.goalId = goalId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    
    public GoalStatus getStatus() { return status; }
    public void setStatus(GoalStatus status) { this.status = status; }
    
    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public java.util.List<String> getTags() { return tags; }
    public void setTags(java.util.List<String> tags) { this.tags = tags; }
}