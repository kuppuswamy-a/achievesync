package com.achievesync.goalservice.aggregate;

import com.achievesync.goalservice.command.CompleteGoalCommand;
import com.achievesync.goalservice.command.CreateGoalCommand;
import com.achievesync.goalservice.command.UpdateGoalProgressCommand;
import com.achievesync.goalservice.event.GoalCompletedEvent;
import com.achievesync.goalservice.event.GoalCreatedEvent;
import com.achievesync.goalservice.event.GoalProgressUpdatedEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class GoalAggregateTest {

    private FixtureConfiguration<GoalAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(GoalAggregate.class);
    }

    @Test
    void testCreateGoal() {
        String goalId = "goal123";
        String userId = "user123";
        String description = "Learn Spring Boot";
        LocalDate targetDate = LocalDate.now().plusMonths(3);

        CreateGoalCommand command = new CreateGoalCommand(goalId, userId, description, targetDate);

        fixture.givenNoPriorActivity()
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEvents(new GoalCreatedEvent(goalId, userId, description, targetDate, java.time.Instant.now()));
    }

    @Test
    void testUpdateGoalProgress() {
        String goalId = "goal123";
        String userId = "user123";
        String description = "Learn Spring Boot";
        LocalDate targetDate = LocalDate.now().plusMonths(3);
        double progressPercentage = 50.0;
        String notes = "Halfway there!";

        GoalCreatedEvent createdEvent = new GoalCreatedEvent(goalId, userId, description, targetDate, 
            java.time.Instant.now());

        UpdateGoalProgressCommand command = new UpdateGoalProgressCommand(goalId, progressPercentage, notes);

        fixture.given(createdEvent)
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEvents(new GoalProgressUpdatedEvent(goalId, progressPercentage, notes, 
                    java.time.Instant.now()));
    }

    @Test
    void testCompleteGoal() {
        String goalId = "goal123";
        String userId = "user123";
        String description = "Learn Spring Boot";
        LocalDate targetDate = LocalDate.now().plusMonths(3);

        GoalCreatedEvent createdEvent = new GoalCreatedEvent(goalId, userId, description, targetDate, 
            java.time.Instant.now());

        CompleteGoalCommand command = new CompleteGoalCommand(goalId);

        fixture.given(createdEvent)
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEvents(new GoalCompletedEvent(goalId, userId, java.time.Instant.now()));
    }

    @Test
    void testUpdateProgressOnCompletedGoal_ShouldFail() {
        String goalId = "goal123";
        String userId = "user123";
        String description = "Learn Spring Boot";
        LocalDate targetDate = LocalDate.now().plusMonths(3);

        GoalCreatedEvent createdEvent = new GoalCreatedEvent(goalId, userId, description, targetDate, 
            java.time.Instant.now());
        GoalCompletedEvent completedEvent = new GoalCompletedEvent(goalId, userId, java.time.Instant.now());

        UpdateGoalProgressCommand command = new UpdateGoalProgressCommand(goalId, 75.0, "Should fail");

        fixture.given(createdEvent, completedEvent)
                .when(command)
                .expectException(IllegalStateException.class);
    }

    @Test
    void testCompleteAlreadyCompletedGoal_ShouldFail() {
        String goalId = "goal123";
        String userId = "user123";
        String description = "Learn Spring Boot";
        LocalDate targetDate = LocalDate.now().plusMonths(3);

        GoalCreatedEvent createdEvent = new GoalCreatedEvent(goalId, userId, description, targetDate, 
            java.time.Instant.now());
        GoalCompletedEvent completedEvent = new GoalCompletedEvent(goalId, userId, java.time.Instant.now());

        CompleteGoalCommand command = new CompleteGoalCommand(goalId);

        fixture.given(createdEvent, completedEvent)
                .when(command)
                .expectException(IllegalStateException.class);
    }
}