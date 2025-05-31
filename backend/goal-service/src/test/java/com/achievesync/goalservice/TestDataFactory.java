package com.achievesync.goalservice;

import com.achievesync.goalservice.command.CreateGoalCommand;
import com.achievesync.goalservice.event.GoalCreatedEvent;
import com.achievesync.goalservice.projection.GoalProjection;
import com.achievesync.goalservice.projection.GoalProgressProjection;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestDataFactory {

    public static CreateGoalCommand createGoalCommand() {
        return createGoalCommand("user123", "Test Goal", LocalDate.now().plusMonths(1));
    }

    public static CreateGoalCommand createGoalCommand(String userId, String description, LocalDate targetDate) {
        return new CreateGoalCommand(
            UUID.randomUUID().toString(),
            userId,
            description,
            targetDate
        );
    }

    public static GoalCreatedEvent goalCreatedEvent() {
        return goalCreatedEvent("goal123", "user123", "Test Goal", LocalDate.now().plusMonths(1));
    }

    public static GoalCreatedEvent goalCreatedEvent(String goalId, String userId, String description, LocalDate targetDate) {
        return new GoalCreatedEvent(goalId, userId, description, targetDate, Instant.now());
    }

    public static GoalProjection goalProjection() {
        return goalProjection("goal123", "user123", "Test Goal");
    }

    public static GoalProjection goalProjection(String goalId, String userId, String description) {
        GoalProjection goal = new GoalProjection();
        goal.setGoalId(goalId);
        goal.setUserId(userId);
        goal.setDescription(description);
        goal.setTargetDate(LocalDate.now().plusMonths(1));
        goal.setStatus(com.achievesync.goalservice.projection.GoalStatus.PENDING);
        goal.setProgressPercentage(0.0);
        goal.setCreatedAt(Instant.now());
        goal.setUpdatedAt(Instant.now());
        goal.setCategory("Test");
        goal.setTags(List.of("test", "example"));
        return goal;
    }

    public static GoalProgressProjection goalProgressProjection(String goalId) {
        return goalProgressProjection(goalId, 50.0, "Test progress");
    }

    public static GoalProgressProjection goalProgressProjection(String goalId, double progress, String notes) {
        return new GoalProgressProjection(
            UUID.randomUUID().toString(),
            goalId,
            progress,
            notes,
            Instant.now()
        );
    }

    public static List<GoalProgressProjection> createProgressHistory(String goalId, int days) {
        List<GoalProgressProjection> history = new ArrayList<>();
        
        for (int i = 0; i < days; i++) {
            GoalProgressProjection progress = new GoalProgressProjection(
                UUID.randomUUID().toString(),
                goalId,
                (i + 1) * 10.0, // Progressive increase
                "Day " + (i + 1) + " progress",
                Instant.now().minusSeconds(86400L * (days - i - 1)) // Spread across days
            );
            history.add(progress);
        }
        
        return history;
    }

    public static List<GoalProjection> createMultipleGoals(String userId, int count) {
        List<GoalProjection> goals = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            GoalProjection goal = goalProjection(
                "goal-" + i,
                userId,
                "Goal " + (i + 1)
            );
            goals.add(goal);
        }
        
        return goals;
    }
}