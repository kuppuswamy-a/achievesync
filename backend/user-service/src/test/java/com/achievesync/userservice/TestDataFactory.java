package com.achievesync.userservice;

import com.achievesync.userservice.command.CreateUserCommand;
import com.achievesync.userservice.event.UserCreatedEvent;
import com.achievesync.userservice.projection.ConsistencyPointsProjection;
import com.achievesync.userservice.projection.UserProjection;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestDataFactory {

    public static CreateUserCommand createUserCommand() {
        return createUserCommand("John Doe", "john.doe@example.com", "password123");
    }

    public static CreateUserCommand createUserCommand(String name, String email, String password) {
        return new CreateUserCommand(
            UUID.randomUUID().toString(),
            name,
            email,
            password
        );
    }

    public static UserCreatedEvent userCreatedEvent() {
        return userCreatedEvent("user123", "John Doe", "john.doe@example.com");
    }

    public static UserCreatedEvent userCreatedEvent(String userId, String name, String email) {
        return new UserCreatedEvent(userId, name, email, Instant.now());
    }

    public static UserProjection userProjection() {
        return userProjection("user123", "John Doe", "john.doe@example.com");
    }

    public static UserProjection userProjection(String userId, String name, String email) {
        UserProjection user = new UserProjection();
        user.setUserId(userId);
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash("$2a$10$N9qo8uLOickgx2ZMRZoMye1YzFtYicqABKQQWGZ6vwkZgJIiW6u1i"); // bcrypt hash for "password"
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    public static ConsistencyPointsProjection consistencyPointsProjection() {
        return consistencyPointsProjection("user123", 100);
    }

    public static ConsistencyPointsProjection consistencyPointsProjection(String userId, int totalPoints) {
        return new ConsistencyPointsProjection(userId, totalPoints, Instant.now());
    }

    public static List<UserProjection> createMultipleUsers(int count) {
        List<UserProjection> users = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            UserProjection user = userProjection(
                "user-" + i,
                "User " + (i + 1),
                "user" + (i + 1) + "@example.com"
            );
            users.add(user);
        }
        
        return users;
    }

    public static String validEmail() {
        return "test-" + UUID.randomUUID().toString() + "@example.com";
    }

    public static String validPassword() {
        return "SecurePassword123!";
    }

    public static String hashPassword(String password) {
        // This would normally use BCryptPasswordEncoder, but for testing we can use a fixed hash
        return "$2a$10$N9qo8uLOickgx2ZMRZoMye1YzFtYicqABKQQWGZ6vwkZgJIiW6u1i";
    }
}