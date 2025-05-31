package com.achievesync.userservice.controller;

import com.achievesync.userservice.projection.ConsistencyPointsProjection;
import com.achievesync.userservice.projection.UserProjection;
import com.achievesync.userservice.query.FindUserQuery;
import com.achievesync.userservice.query.GetConsistencyPointsQuery;
import com.achievesync.userservice.service.JwtTokenUtil;
import com.achievesync.userservice.service.JwtUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommandGateway commandGateway;

    @MockBean
    private QueryGateway queryGateway;

    @MockBean
    private JwtUserDetailsService jwtUserDetailsService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void testCreateUser() throws Exception {
        when(commandGateway.send(any())).thenReturn(CompletableFuture.completedFuture(null));

        UserController.CreateUserRequest request = new UserController.CreateUserRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("securePassword123");

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void testGetUser() throws Exception {
        String userId = "user123";
        UserProjection user = createTestUser(userId, "John Doe", "john.doe@example.com");

        when(queryGateway.query(any(FindUserQuery.class), eq(UserProjection.class)))
                .thenReturn(CompletableFuture.completedFuture(user));

        mockMvc.perform(get("/api/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @WithMockUser
    void testGetUser_NotFound() throws Exception {
        String userId = "nonexistent";

        when(queryGateway.query(any(FindUserQuery.class), eq(UserProjection.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(get("/api/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testUpdateUser() throws Exception {
        String userId = "user123";
        when(commandGateway.send(any())).thenReturn(CompletableFuture.completedFuture(null));

        UserController.UpdateUserRequest request = new UserController.UpdateUserRequest();
        request.setName("John Smith");
        request.setEmail("john.smith@example.com");

        mockMvc.perform(put("/api/users/{userId}", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testAwardPoints() throws Exception {
        String userId = "user123";
        when(commandGateway.send(any())).thenReturn(CompletableFuture.completedFuture(null));

        UserController.AwardPointsRequest request = new UserController.AwardPointsRequest();
        request.setPoints(50);
        request.setReason("Completed daily goal");

        mockMvc.perform(post("/api/users/{userId}/points", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetPoints() throws Exception {
        String userId = "user123";
        ConsistencyPointsProjection points = createTestConsistencyPoints(userId, 150);

        when(queryGateway.query(any(GetConsistencyPointsQuery.class), eq(ConsistencyPointsProjection.class)))
                .thenReturn(CompletableFuture.completedFuture(points));

        mockMvc.perform(get("/api/users/{userId}/points", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalPoints").value(150));
    }

    @Test
    @WithMockUser
    void testGetPoints_NotFound() throws Exception {
        String userId = "nonexistent";

        when(queryGateway.query(any(GetConsistencyPointsQuery.class), eq(ConsistencyPointsProjection.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(get("/api/users/{userId}/points", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUser_ValidationErrors() throws Exception {
        UserController.CreateUserRequest request = new UserController.CreateUserRequest();
        // Empty request should trigger validation errors

        mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testAwardPoints_NegativePoints() throws Exception {
        String userId = "user123";

        UserController.AwardPointsRequest request = new UserController.AwardPointsRequest();
        request.setPoints(-10);
        request.setReason("Should not allow negative points");

        mockMvc.perform(post("/api/users/{userId}/points", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private UserProjection createTestUser(String userId, String name, String email) {
        UserProjection user = new UserProjection();
        user.setUserId(userId);
        user.setName(name);
        user.setEmail(email);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    private ConsistencyPointsProjection createTestConsistencyPoints(String userId, int totalPoints) {
        return new ConsistencyPointsProjection(userId, totalPoints, Instant.now());
    }
}