package com.achievesync.goalservice.service;

import com.achievesync.goalservice.command.CreateGoalCommand;
import com.achievesync.goalservice.command.UpdateGoalProgressCommand;
import com.achievesync.goalservice.command.CompleteGoalCommand;
import com.achievesync.goalservice.projection.GoalProjection;
import com.achievesync.goalservice.projection.GoalProgressProjection;
import com.achievesync.goalservice.query.FindGoalQuery;
import com.achievesync.goalservice.query.FindGoalsByUserQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class GoalService {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private QueryGateway queryGateway;

    @Autowired
    private StreakCalculationService streakCalculationService;

    public CompletableFuture<String> createGoal(String userId, String description, LocalDate targetDate, 
                                               String category, List<String> tags) {
        String goalId = UUID.randomUUID().toString();
        CreateGoalCommand command = new CreateGoalCommand(goalId, userId, description, targetDate, category, tags);
        
        return commandGateway.send(command).thenApply(result -> goalId);
    }

    public CompletableFuture<Void> updateGoalProgress(String goalId, double progressPercentage, String notes) {
        UpdateGoalProgressCommand command = new UpdateGoalProgressCommand(goalId, progressPercentage, notes);
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> completeGoal(String goalId) {
        CompleteGoalCommand command = new CompleteGoalCommand(goalId);
        return commandGateway.send(command);
    }

    public CompletableFuture<GoalProjection> getGoal(String goalId) {
        return queryGateway.query(new FindGoalQuery(goalId), GoalProjection.class);
    }

    public CompletableFuture<List<GoalProjection>> getUserGoals(String userId) {
        return queryGateway.query(
            new FindGoalsByUserQuery(userId), 
            org.axonframework.messaging.responsetypes.ResponseTypes.multipleInstancesOf(GoalProjection.class)
        );
    }

    public StreakCalculationService.StreakData calculateGoalStreak(String goalId, 
                                                                  List<GoalProgressProjection> progressHistory) {
        return streakCalculationService.calculateStreak(progressHistory);
    }
}