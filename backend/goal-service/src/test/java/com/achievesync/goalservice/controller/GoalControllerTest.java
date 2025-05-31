package com.achievesync.goalservice.controller;

import com.achievesync.goalservice.projection.GoalProjection;
import com.achievesync.goalservice.query.FindGoalQuery;
import com.achievesync.goalservice.query.FindGoalsByUserQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalController.class)
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommandGateway commandGateway;

    @MockBean
    private QueryGateway queryGateway;

    @Test
    void testCreateGoal() throws Exception {
        when(commandGateway.send(any())).thenReturn(CompletableFuture.completedFuture(null));

        GoalController.CreateGoalRequest request = new GoalController.CreateGoalRequest();
        request.setUserId("user123");
        request.setDescription("Learn Spring Boot");
        request.setTargetDate(LocalDate.now().plusMonths(3));

        mockMvc.perform(post("/api/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetGoal() throws Exception {
        String goalId = "goal123";
        GoalProjection goal = createTestGoal(goalId, "user123", "Learn Spring Boot");

        when(queryGateway.query(any(FindGoalQuery.class), eq(GoalProjection.class)))
                .thenReturn(CompletableFuture.completedFuture(goal));

        mockMvc.perform(get("/api/goals/{goalId}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalId").value(goalId))
                .andExpect(jsonPath("$.description").value("Learn Spring Boot"));
    }

    @Test
    void testGetGoal_NotFound() throws Exception {
        String goalId = "nonexistent";

        when(queryGateway.query(any(FindGoalQuery.class), eq(GoalProjection.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(get("/api/goals/{goalId}", goalId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserGoals() throws Exception {
        String userId = "user123";
        GoalProjection goal1 = createTestGoal("goal1", userId, "Learn Spring Boot");
        GoalProjection goal2 = createTestGoal("goal2", userId, "Build REST API");

        when(queryGateway.query(any(FindGoalsByUserQuery.class), any(org.axonframework.messaging.responsetypes.ResponseType.class)))
                .thenReturn(CompletableFuture.completedFuture(Arrays.asList(goal1, goal2)));

        mockMvc.perform(get("/api/goals/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testUpdateProgress() throws Exception {
        String goalId = "goal123";
        when(commandGateway.send(any())).thenReturn(CompletableFuture.completedFuture(null));

        GoalController.UpdateProgressRequest request = new GoalController.UpdateProgressRequest();
        request.setProgressPercentage(75.0);
        request.setNotes("Making good progress");

        mockMvc.perform(put("/api/goals/{goalId}/progress", goalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testCompleteGoal() throws Exception {
        String goalId = "goal123";
        when(commandGateway.send(any())).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(put("/api/goals/{goalId}/complete", goalId))
                .andExpect(status().isOk());
    }

    private GoalProjection createTestGoal(String goalId, String userId, String description) {
        GoalProjection goal = new GoalProjection();
        goal.setGoalId(goalId);
        goal.setUserId(userId);
        goal.setDescription(description);
        goal.setTargetDate(LocalDate.now().plusMonths(3));
        goal.setStatus(com.achievesync.goalservice.projection.GoalStatus.PENDING);
        goal.setProgressPercentage(0.0);
        goal.setCreatedAt(Instant.now());
        goal.setUpdatedAt(Instant.now());
        return goal;
    }
}