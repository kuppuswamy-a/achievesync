package com.achievesync.goalservice.controller;

import com.achievesync.goalservice.command.CompleteGoalCommand;
import com.achievesync.goalservice.command.CreateGoalCommand;
import com.achievesync.goalservice.command.UpdateGoalProgressCommand;
import com.achievesync.goalservice.projection.GoalProjection;
import com.achievesync.goalservice.query.FindGoalQuery;
import com.achievesync.goalservice.query.FindGoalsByUserQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/goals")
@CrossOrigin(origins = "*")
public class GoalController {
    
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public GoalController(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<String>> createGoal(@RequestBody CreateGoalRequest request) {
        String goalId = UUID.randomUUID().toString();
        CreateGoalCommand command = new CreateGoalCommand(
            goalId,
            request.getUserId(),
            request.getDescription(),
            request.getTargetDate()
        );
        
        return commandGateway.send(command)
            .thenApply(result -> ResponseEntity.status(HttpStatus.CREATED).body(goalId));
    }

    @GetMapping("/{goalId}")
    public CompletableFuture<ResponseEntity<GoalProjection>> getGoal(@PathVariable String goalId) {
        return queryGateway.query(new FindGoalQuery(goalId), GoalProjection.class)
            .thenApply(goal -> goal != null ? 
                ResponseEntity.ok(goal) : 
                ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public CompletableFuture<ResponseEntity<List<GoalProjection>>> getUserGoals(@PathVariable String userId) {
        return queryGateway.query(
            new FindGoalsByUserQuery(userId), 
            org.axonframework.messaging.responsetypes.ResponseTypes.multipleInstancesOf(GoalProjection.class)
        ).thenApply(goals -> ResponseEntity.ok(goals));
    }

    @PutMapping("/{goalId}/progress")
    public CompletableFuture<ResponseEntity<Void>> updateProgress(@PathVariable String goalId, 
                                                                @RequestBody UpdateProgressRequest request) {
        UpdateGoalProgressCommand command = new UpdateGoalProgressCommand(
            goalId,
            request.getProgressPercentage(),
            request.getNotes()
        );
        
        return commandGateway.send(command)
            .thenApply(result -> ResponseEntity.ok().build());
    }

    @PutMapping("/{goalId}/complete")
    public CompletableFuture<ResponseEntity<Void>> completeGoal(@PathVariable String goalId) {
        CompleteGoalCommand command = new CompleteGoalCommand(goalId);
        
        return commandGateway.send(command)
            .thenApply(result -> ResponseEntity.ok().build());
    }

    // Request DTOs
    public static class CreateGoalRequest {
        private String userId;
        private String description;
        private LocalDate targetDate;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    }

    public static class UpdateProgressRequest {
        private double progressPercentage;
        private String notes;

        public double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}