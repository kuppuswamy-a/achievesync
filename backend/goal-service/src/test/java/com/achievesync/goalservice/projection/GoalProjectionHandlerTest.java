package com.achievesync.goalservice.projection;

import com.achievesync.goalservice.event.GoalCompletedEvent;
import com.achievesync.goalservice.event.GoalCreatedEvent;
import com.achievesync.goalservice.event.GoalProgressUpdatedEvent;
import com.achievesync.goalservice.query.FindGoalQuery;
import com.achievesync.goalservice.query.FindGoalsByUserQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalProjectionHandlerTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalProgressRepository progressRepository;

    private GoalProjectionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GoalProjectionHandler(goalRepository, progressRepository);
    }

    @Test
    void testHandleGoalCreatedEvent() {
        String goalId = "goal123";
        String userId = "user123";
        String description = "Learn Spring Boot";
        LocalDate targetDate = LocalDate.now().plusMonths(3);
        Instant createdAt = Instant.now();

        GoalCreatedEvent event = new GoalCreatedEvent(goalId, userId, description, targetDate, createdAt);

        handler.on(event);

        verify(goalRepository).save(any(GoalProjection.class));
        verify(goalRepository).save(argThat(goal -> 
            goal.getGoalId().equals(goalId) &&
            goal.getUserId().equals(userId) &&
            goal.getDescription().equals(description) &&
            goal.getTargetDate().equals(targetDate) &&
            goal.getStatus() == com.achievesync.goalservice.projection.GoalStatus.PENDING &&
            goal.getProgressPercentage() == 0.0
        ));
    }

    @Test
    void testHandleGoalProgressUpdatedEvent() {
        String goalId = "goal123";
        double progressPercentage = 75.0;
        String notes = "Making progress";
        Instant updatedAt = Instant.now();

        GoalProjection existingGoal = new GoalProjection();
        existingGoal.setGoalId(goalId);
        existingGoal.setStatus(com.achievesync.goalservice.projection.GoalStatus.IN_PROGRESS);
        existingGoal.setProgressPercentage(50.0);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(existingGoal));

        GoalProgressUpdatedEvent event = new GoalProgressUpdatedEvent(goalId, progressPercentage, notes, updatedAt);

        handler.on(event);

        verify(goalRepository).save(argThat(goal -> 
            goal.getProgressPercentage() == progressPercentage &&
            goal.getStatus() == com.achievesync.goalservice.projection.GoalStatus.IN_PROGRESS &&
            goal.getUpdatedAt().equals(updatedAt)
        ));

        verify(progressRepository).save(any(GoalProgressProjection.class));
        verify(progressRepository).save(argThat(progress ->
            progress.getGoalId().equals(goalId) &&
            progress.getProgressPercentage() == progressPercentage &&
            progress.getNotes().equals(notes)
        ));
    }

    @Test
    void testHandleGoalProgressUpdatedEvent_CompletesGoal() {
        String goalId = "goal123";
        double progressPercentage = 100.0;
        String notes = "Goal completed!";
        Instant updatedAt = Instant.now();

        GoalProjection existingGoal = new GoalProjection();
        existingGoal.setGoalId(goalId);
        existingGoal.setStatus(com.achievesync.goalservice.projection.GoalStatus.IN_PROGRESS);
        existingGoal.setProgressPercentage(90.0);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(existingGoal));

        GoalProgressUpdatedEvent event = new GoalProgressUpdatedEvent(goalId, progressPercentage, notes, updatedAt);

        handler.on(event);

        verify(goalRepository).save(argThat(goal -> 
            goal.getProgressPercentage() == progressPercentage &&
            goal.getStatus() == com.achievesync.goalservice.projection.GoalStatus.COMPLETED
        ));
    }

    @Test
    void testHandleGoalCompletedEvent() {
        String goalId = "goal123";
        String userId = "user123";
        Instant completedAt = Instant.now();

        GoalProjection existingGoal = new GoalProjection();
        existingGoal.setGoalId(goalId);
        existingGoal.setUserId(userId);
        existingGoal.setStatus(com.achievesync.goalservice.projection.GoalStatus.IN_PROGRESS);
        existingGoal.setProgressPercentage(95.0);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(existingGoal));

        GoalCompletedEvent event = new GoalCompletedEvent(goalId, userId, completedAt);

        handler.on(event);

        verify(goalRepository).save(argThat(goal -> 
            goal.getStatus() == com.achievesync.goalservice.projection.GoalStatus.COMPLETED &&
            goal.getProgressPercentage() == 100.0 &&
            goal.getUpdatedAt().equals(completedAt)
        ));
    }

    @Test
    void testHandleFindGoalQuery() {
        String goalId = "goal123";
        GoalProjection expectedGoal = new GoalProjection();
        expectedGoal.setGoalId(goalId);

        when(goalRepository.findById(goalId)).thenReturn(Optional.of(expectedGoal));

        FindGoalQuery query = new FindGoalQuery(goalId);
        GoalProjection result = handler.handle(query);

        assertEquals(expectedGoal, result);
        verify(goalRepository).findById(goalId);
    }

    @Test
    void testHandleFindGoalQuery_NotFound() {
        String goalId = "nonexistent";

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        FindGoalQuery query = new FindGoalQuery(goalId);
        GoalProjection result = handler.handle(query);

        assertNull(result);
        verify(goalRepository).findById(goalId);
    }

    @Test
    void testHandleFindGoalsByUserQuery() {
        String userId = "user123";
        GoalProjection goal1 = new GoalProjection();
        goal1.setGoalId("goal1");
        goal1.setUserId(userId);

        GoalProjection goal2 = new GoalProjection();
        goal2.setGoalId("goal2");
        goal2.setUserId(userId);

        List<GoalProjection> expectedGoals = Arrays.asList(goal1, goal2);

        when(goalRepository.findByUserId(userId)).thenReturn(expectedGoals);

        FindGoalsByUserQuery query = new FindGoalsByUserQuery(userId);
        List<GoalProjection> result = handler.handle(query);

        assertEquals(expectedGoals, result);
        verify(goalRepository).findByUserId(userId);
    }

    @Test
    void testHandleGoalProgressUpdatedEvent_GoalNotFound() {
        String goalId = "nonexistent";
        GoalProgressUpdatedEvent event = new GoalProgressUpdatedEvent(goalId, 50.0, "notes", Instant.now());

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        handler.on(event);

        verify(goalRepository, never()).save(any(GoalProjection.class));
        verify(progressRepository, never()).save(any(GoalProgressProjection.class));
    }

    @Test
    void testHandleGoalCompletedEvent_GoalNotFound() {
        String goalId = "nonexistent";
        GoalCompletedEvent event = new GoalCompletedEvent(goalId, "user123", Instant.now());

        when(goalRepository.findById(goalId)).thenReturn(Optional.empty());

        handler.on(event);

        verify(goalRepository, never()).save(any(GoalProjection.class));
    }
}