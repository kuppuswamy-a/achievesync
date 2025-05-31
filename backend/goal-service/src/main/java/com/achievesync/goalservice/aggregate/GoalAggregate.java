package com.achievesync.goalservice.aggregate;

import com.achievesync.goalservice.command.CompleteGoalCommand;
import com.achievesync.goalservice.command.CreateGoalCommand;
import com.achievesync.goalservice.command.UpdateGoalProgressCommand;
import com.achievesync.goalservice.event.GoalCompletedEvent;
import com.achievesync.goalservice.event.GoalCreatedEvent;
import com.achievesync.goalservice.event.GoalProgressUpdatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Aggregate
public class GoalAggregate {
    
    @AggregateIdentifier
    private String goalId;
    private String userId;
    private String description;
    private LocalDate targetDate;
    private GoalStatus status;
    private double progressPercentage;
    private Instant createdAt;
    private Instant updatedAt;
    private String category;
    private List<String> tags;

    public GoalAggregate() {
        // Required by Axon
    }

    @CommandHandler
    public GoalAggregate(CreateGoalCommand command) {
        AggregateLifecycle.apply(new GoalCreatedEvent(
            command.getGoalId(),
            command.getUserId(),
            command.getDescription(),
            command.getTargetDate(),
            Instant.now(),
            command.getCategory(),
            command.getTags()
        ));
    }

    @CommandHandler
    public void handle(UpdateGoalProgressCommand command) {
        if (this.status == GoalStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update progress of completed goal");
        }
        
        AggregateLifecycle.apply(new GoalProgressUpdatedEvent(
            command.getGoalId(),
            command.getProgressPercentage(),
            command.getNotes(),
            Instant.now()
        ));
    }

    @CommandHandler
    public void handle(CompleteGoalCommand command) {
        if (this.status == GoalStatus.COMPLETED) {
            throw new IllegalStateException("Goal is already completed");
        }
        
        AggregateLifecycle.apply(new GoalCompletedEvent(
            command.getGoalId(),
            this.userId,
            Instant.now()
        ));
    }

    @EventSourcingHandler
    public void on(GoalCreatedEvent event) {
        this.goalId = event.getGoalId();
        this.userId = event.getUserId();
        this.description = event.getDescription();
        this.targetDate = event.getTargetDate();
        this.status = GoalStatus.PENDING;
        this.progressPercentage = 0.0;
        this.createdAt = event.getCreatedAt();
        this.updatedAt = event.getCreatedAt();
        this.category = event.getCategory();
        this.tags = event.getTags();
    }

    @EventSourcingHandler
    public void on(GoalProgressUpdatedEvent event) {
        this.progressPercentage = event.getProgressPercentage();
        this.status = event.getProgressPercentage() >= 100.0 ? GoalStatus.COMPLETED : GoalStatus.IN_PROGRESS;
        this.updatedAt = event.getUpdatedAt();
    }

    @EventSourcingHandler
    public void on(GoalCompletedEvent event) {
        this.status = GoalStatus.COMPLETED;
        this.progressPercentage = 100.0;
        this.updatedAt = event.getCompletedAt();
    }

    // Getters
    public String getGoalId() { return goalId; }
    public String getUserId() { return userId; }
    public String getDescription() { return description; }
    public LocalDate getTargetDate() { return targetDate; }
    public GoalStatus getStatus() { return status; }
    public double getProgressPercentage() { return progressPercentage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getCategory() { return category; }
    public List<String> getTags() { return tags; }
}

enum GoalStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}