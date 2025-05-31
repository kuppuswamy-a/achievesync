package com.achievesync.userservice.integration;

import com.achievesync.userservice.config.TestAxonConfig;
import com.achievesync.userservice.controller.UserController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration,net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration,net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration",
    "axon.axonserver.enabled=false"
})
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(TestAxonConfig.class)
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCompleteUserLifecycle() throws Exception {
        String email = "integration-" + UUID.randomUUID().toString() + "@example.com";
        String name = "Integration Test User";
        String password = "securePassword123";

        // Step 1: Create user
        UserController.CreateUserRequest createRequest = new UserController.CreateUserRequest();
        createRequest.setName(name);
        createRequest.setEmail(email);
        createRequest.setPassword(password);

        String userId = mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("\"", "");

        Thread.sleep(100); // Allow for async processing

        // Step 2: Retrieve user
        mockMvc.perform(get("/api/users/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email));

        // Step 3: Update user profile
        String updatedName = "Updated Integration User";
        String updatedEmail = "updated-" + UUID.randomUUID().toString() + "@example.com";

        UserController.UpdateUserRequest updateRequest = new UserController.UpdateUserRequest();
        updateRequest.setName(updatedName);
        updateRequest.setEmail(updatedEmail);

        mockMvc.perform(put("/api/users/{userId}", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        Thread.sleep(100); // Allow for async processing

        // Step 4: Verify profile was updated
        mockMvc.perform(get("/api/users/{userId}", userId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedName))
                .andExpect(jsonPath("$.email").value(updatedEmail));

        // Step 5: Award consistency points
        int initialPoints = 50;
        UserController.AwardPointsRequest pointsRequest = new UserController.AwardPointsRequest();
        pointsRequest.setPoints(initialPoints);
        pointsRequest.setReason("Initial achievement");

        mockMvc.perform(post("/api/users/{userId}/points", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pointsRequest)))
                .andExpect(status().isOk());

        Thread.sleep(100); // Allow for async processing

        // Step 6: Verify points were awarded
        mockMvc.perform(get("/api/users/{userId}/points", userId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalPoints").value(initialPoints));

        // Step 7: Award additional points
        int additionalPoints = 25;
        UserController.AwardPointsRequest morePointsRequest = new UserController.AwardPointsRequest();
        morePointsRequest.setPoints(additionalPoints);
        morePointsRequest.setReason("Additional achievement");

        mockMvc.perform(post("/api/users/{userId}/points", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(morePointsRequest)))
                .andExpect(status().isOk());

        Thread.sleep(100); // Allow for async processing

        // Step 8: Verify points were accumulated
        mockMvc.perform(get("/api/users/{userId}/points", userId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(initialPoints + additionalPoints));
    }

    @Test
    void testMultipleUsersCreation() throws Exception {
        int numberOfUsers = 5;
        String[] userIds = new String[numberOfUsers];

        // Create multiple users
        for (int i = 0; i < numberOfUsers; i++) {
            String email = "multi-user-" + i + "-" + UUID.randomUUID().toString() + "@example.com";
            String name = "Multi User " + i;

            UserController.CreateUserRequest createRequest = new UserController.CreateUserRequest();
            createRequest.setName(name);
            createRequest.setEmail(email);
            createRequest.setPassword("password" + i);

            userIds[i] = mockMvc.perform(post("/api/users")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString()
                    .replaceAll("\"", "");
        }

        Thread.sleep(200); // Allow for async processing

        // Verify all users were created
        for (int i = 0; i < numberOfUsers; i++) {
            mockMvc.perform(get("/api/users/{userId}", userIds[i])
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userIds[i]))
                    .andExpect(jsonPath("$.name").value("Multi User " + i));
        }
    }

    @Test
    void testConsistencyPointsAccumulation() throws Exception {
        // Create user
        String email = "points-user-" + UUID.randomUUID().toString() + "@example.com";

        UserController.CreateUserRequest createRequest = new UserController.CreateUserRequest();
        createRequest.setName("Points Test User");
        createRequest.setEmail(email);
        createRequest.setPassword("password");

        String userId = mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("\"", "");

        Thread.sleep(100);

        // Award points multiple times
        int[] pointsToAward = {10, 25, 15, 30, 20};
        int expectedTotal = 0;

        for (int i = 0; i < pointsToAward.length; i++) {
            expectedTotal += pointsToAward[i];

            UserController.AwardPointsRequest pointsRequest = new UserController.AwardPointsRequest();
            pointsRequest.setPoints(pointsToAward[i]);
            pointsRequest.setReason("Achievement " + (i + 1));

            mockMvc.perform(post("/api/users/{userId}/points", userId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(pointsRequest)))
                    .andExpect(status().isOk());

            Thread.sleep(50); // Small delay between awards
        }

        Thread.sleep(100); // Final processing time

        // Verify total points
        mockMvc.perform(get("/api/users/{userId}/points", userId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(expectedTotal));
    }

    @Test
    void testConcurrentPointsAwarding() throws Exception {
        // Create user
        String email = "concurrent-points-" + UUID.randomUUID().toString() + "@example.com";

        UserController.CreateUserRequest createRequest = new UserController.CreateUserRequest();
        createRequest.setName("Concurrent Points User");
        createRequest.setEmail(email);
        createRequest.setPassword("password");

        String userId = mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("\"", "");

        Thread.sleep(100);

        // Award points concurrently
        java.util.concurrent.CompletableFuture<Void> award1 = java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                UserController.AwardPointsRequest pointsRequest = new UserController.AwardPointsRequest();
                pointsRequest.setPoints(25);
                pointsRequest.setReason("Concurrent award 1");

                mockMvc.perform(post("/api/users/{userId}/points", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pointsRequest)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        java.util.concurrent.CompletableFuture<Void> award2 = java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                UserController.AwardPointsRequest pointsRequest = new UserController.AwardPointsRequest();
                pointsRequest.setPoints(35);
                pointsRequest.setReason("Concurrent award 2");

                mockMvc.perform(post("/api/users/{userId}/points", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pointsRequest)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Wait for both awards to complete
        java.util.concurrent.CompletableFuture.allOf(award1, award2).join();
        Thread.sleep(200); // Allow for event processing

        // Verify points were properly accumulated
        mockMvc.perform(get("/api/users/{userId}/points", userId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(60)); // 25 + 35
    }

    @Test
    void testUserNotFound() throws Exception {
        String nonExistentUserId = "non-existent-" + UUID.randomUUID().toString();

        mockMvc.perform(get("/api/users/{userId}", nonExistentUserId)
                .with(csrf()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/users/{userId}/points", nonExistentUserId)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testInvalidUserData() throws Exception {
        // Test with empty name
        UserController.CreateUserRequest invalidRequest = new UserController.CreateUserRequest();
        invalidRequest.setName(""); // Empty name
        invalidRequest.setEmail("invalid@example.com");
        invalidRequest.setPassword("password");

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Test with invalid email
        UserController.CreateUserRequest invalidEmailRequest = new UserController.CreateUserRequest();
        invalidEmailRequest.setName("Valid Name");
        invalidEmailRequest.setEmail("not-an-email"); // Invalid email format
        invalidEmailRequest.setPassword("password");

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest());
    }
}