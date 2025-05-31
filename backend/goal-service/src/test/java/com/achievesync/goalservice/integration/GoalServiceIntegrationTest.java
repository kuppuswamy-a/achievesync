package com.achievesync.goalservice.integration;

import com.achievesync.goalservice.command.CreateGoalCommand;
import com.achievesync.goalservice.command.UpdateGoalProgressCommand;
import com.achievesync.goalservice.config.TestAxonConfig;
import com.achievesync.goalservice.controller.GoalController;
import com.achievesync.goalservice.projection.GoalProjection;
import com.achievesync.goalservice.query.FindGoalQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    properties = {
        "spring.autoconfigure.exclude=net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration,net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration,net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration",
        "axon.axonserver.enabled=false"
    },
    classes = {
        com.achievesync.goalservice.GoalServiceApplication.class
    }
)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(TestAxonConfig.class)
@Transactional
class GoalServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private QueryGateway queryGateway;

    @Test
    void testCompleteGoalWorkflow() throws Exception {
        // Step 1: Create a goal
        String userId = "integration-user-" + UUID.randomUUID().toString();
        String description = "Integration Test Goal";
        LocalDate targetDate = LocalDate.now().plusMonths(1);

        GoalController.CreateGoalRequest createRequest = new GoalController.CreateGoalRequest();
        createRequest.setUserId(userId);
        createRequest.setDescription(description);
        createRequest.setTargetDate(targetDate);

        String response = mockMvc.perform(post("/api/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String goalId = response.replaceAll("\"", ""); // Remove quotes

        // Step 2: Verify goal was created
        mockMvc.perform(get("/api/goals/{goalId}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalId").value(goalId))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.progressPercentage").value(0.0));

        // Step 3: Update progress
        GoalController.UpdateProgressRequest progressRequest = new GoalController.UpdateProgressRequest();
        progressRequest.setProgressPercentage(50.0);
        progressRequest.setNotes("Halfway there!");

        mockMvc.perform(put("/api/goals/{goalId}/progress", goalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(progressRequest)))
                .andExpect(status().isOk());

        // Step 4: Verify progress was updated
        Thread.sleep(100); // Allow for async processing

        mockMvc.perform(get("/api/goals/{goalId}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPercentage").value(50.0));

        // Step 5: Complete the goal
        mockMvc.perform(put("/api/goals/{goalId}/complete", goalId))
                .andExpect(status().isOk());

        // Step 6: Verify goal was completed
        Thread.sleep(100); // Allow for async processing

        mockMvc.perform(get("/api/goals/{goalId}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.progressPercentage").value(100.0));
    }

    @Test
    void testGetUserGoals() throws Exception {
        String userId = "integration-user-goals-" + UUID.randomUUID().toString();

        // Create multiple goals for the same user
        for (int i = 1; i <= 3; i++) {
            GoalController.CreateGoalRequest createRequest = new GoalController.CreateGoalRequest();
            createRequest.setUserId(userId);
            createRequest.setDescription("Goal " + i);
            createRequest.setTargetDate(LocalDate.now().plusMonths(i));

            mockMvc.perform(post("/api/goals")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated());
        }

        // Allow time for event processing
        Thread.sleep(200);

        // Retrieve all goals for the user
        mockMvc.perform(get("/api/goals/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void testGoalProgressTracking() throws Exception {
        String userId = "progress-user-" + UUID.randomUUID().toString();
        
        // Create goal
        GoalController.CreateGoalRequest createRequest = new GoalController.CreateGoalRequest();
        createRequest.setUserId(userId);
        createRequest.setDescription("Progress Tracking Test");
        createRequest.setTargetDate(LocalDate.now().plusMonths(1));

        String goalId = mockMvc.perform(post("/api/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("\"", "");

        // Track multiple progress updates
        double[] progressValues = {25.0, 50.0, 75.0, 90.0};
        
        for (double progress : progressValues) {
            GoalController.UpdateProgressRequest progressRequest = new GoalController.UpdateProgressRequest();
            progressRequest.setProgressPercentage(progress);
            progressRequest.setNotes("Progress: " + progress + "%");

            mockMvc.perform(put("/api/goals/{goalId}/progress", goalId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(progressRequest)))
                    .andExpect(status().isOk());

            Thread.sleep(50); // Small delay between updates
        }

        // Verify final progress
        mockMvc.perform(get("/api/goals/{goalId}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPercentage").value(90.0))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void testAutoCompleteOn100Percent() throws Exception {
        String userId = "auto-complete-user-" + UUID.randomUUID().toString();
        
        // Create goal
        GoalController.CreateGoalRequest createRequest = new GoalController.CreateGoalRequest();
        createRequest.setUserId(userId);
        createRequest.setDescription("Auto Complete Test");
        createRequest.setTargetDate(LocalDate.now().plusMonths(1));

        String goalId = mockMvc.perform(post("/api/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("\"", "");

        // Update progress to 100%
        GoalController.UpdateProgressRequest progressRequest = new GoalController.UpdateProgressRequest();
        progressRequest.setProgressPercentage(100.0);
        progressRequest.setNotes("Completed!");

        mockMvc.perform(put("/api/goals/{goalId}/progress", goalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(progressRequest)))
                .andExpect(status().isOk());

        Thread.sleep(100); // Allow for async processing

        // Verify goal is automatically marked as completed
        mockMvc.perform(get("/api/goals/{goalId}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.progressPercentage").value(100.0));
    }

    @Test
    void testConcurrentGoalUpdates() throws Exception {
        String userId = "concurrent-user-" + UUID.randomUUID().toString();
        
        // Create goal
        GoalController.CreateGoalRequest createRequest = new GoalController.CreateGoalRequest();
        createRequest.setUserId(userId);
        createRequest.setDescription("Concurrent Updates Test");
        createRequest.setTargetDate(LocalDate.now().plusMonths(1));

        String goalId = mockMvc.perform(post("/api/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("\"", "");

        // Simulate concurrent updates
        CompletableFuture<Void> update1 = CompletableFuture.runAsync(() -> {
            try {
                GoalController.UpdateProgressRequest progressRequest = new GoalController.UpdateProgressRequest();
                progressRequest.setProgressPercentage(30.0);
                progressRequest.setNotes("Concurrent update 1");

                mockMvc.perform(put("/api/goals/{goalId}/progress", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progressRequest)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<Void> update2 = CompletableFuture.runAsync(() -> {
            try {
                GoalController.UpdateProgressRequest progressRequest = new GoalController.UpdateProgressRequest();
                progressRequest.setProgressPercentage(60.0);
                progressRequest.setNotes("Concurrent update 2");

                mockMvc.perform(put("/api/goals/{goalId}/progress", goalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progressRequest)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for both updates to complete
        CompletableFuture.allOf(update1, update2).join();
        Thread.sleep(200); // Allow for event processing

        // Verify goal still exists and has been updated
        mockMvc.perform(get("/api/goals/{goalId}", goalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalId").value(goalId));
    }
}