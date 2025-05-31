package com.achievesync.userservice.aggregate;

import com.achievesync.userservice.command.AwardConsistencyPointsCommand;
import com.achievesync.userservice.command.CreateUserCommand;
import com.achievesync.userservice.command.UpdateUserProfileCommand;
import com.achievesync.userservice.event.ConsistencyPointsAwardedEvent;
import com.achievesync.userservice.event.UserCreatedEvent;
import com.achievesync.userservice.event.UserProfileUpdatedEvent;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class UserAggregateTest {

    private FixtureConfiguration<UserAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(UserAggregate.class);
    }

    @Test
    void testCreateUser() {
        String userId = "user123";
        String name = "John Doe";
        String email = "john.doe@example.com";
        String password = "securePassword123";

        CreateUserCommand command = new CreateUserCommand(userId, name, email, password);

        fixture.givenNoPriorActivity()
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEvents(new UserCreatedEvent(userId, name, email, Instant.now()));
    }

    @Test
    void testUpdateUserProfile() {
        String userId = "user123";
        String originalName = "John Doe";
        String originalEmail = "john.doe@example.com";
        String updatedName = "John Smith";
        String updatedEmail = "john.smith@example.com";

        UserCreatedEvent createdEvent = new UserCreatedEvent(userId, originalName, originalEmail, Instant.now());
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(userId, updatedName, updatedEmail);

        fixture.given(createdEvent)
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEvents(new UserProfileUpdatedEvent(userId, updatedName, updatedEmail, Instant.now()));
    }

    @Test
    void testAwardConsistencyPoints() {
        String userId = "user123";
        String name = "John Doe";
        String email = "john.doe@example.com";
        int pointsToAward = 50;
        String reason = "Completed daily goal";

        UserCreatedEvent createdEvent = new UserCreatedEvent(userId, name, email, Instant.now());
        AwardConsistencyPointsCommand command = new AwardConsistencyPointsCommand(userId, pointsToAward, reason);

        fixture.given(createdEvent)
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEvents(new ConsistencyPointsAwardedEvent(userId, pointsToAward, pointsToAward, reason, Instant.now()));
    }

    @Test
    void testAwardConsistencyPoints_AccumulatesPoints() {
        String userId = "user123";
        String name = "John Doe";
        String email = "john.doe@example.com";
        int initialPoints = 25;
        int additionalPoints = 30;
        int expectedTotal = initialPoints + additionalPoints;

        UserCreatedEvent createdEvent = new UserCreatedEvent(userId, name, email, Instant.now());
        ConsistencyPointsAwardedEvent firstAward = new ConsistencyPointsAwardedEvent(
            userId, initialPoints, initialPoints, "First achievement", Instant.now());

        AwardConsistencyPointsCommand command = new AwardConsistencyPointsCommand(
            userId, additionalPoints, "Second achievement");

        fixture.given(createdEvent, firstAward)
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEvents(new ConsistencyPointsAwardedEvent(
                    userId, additionalPoints, expectedTotal, "Second achievement", Instant.now()));
    }

    @Test
    void testMultipleOperationsOnSameUser() {
        String userId = "user123";
        String name = "John Doe";
        String email = "john.doe@example.com";
        String updatedName = "John Smith";
        int points = 100;

        UserCreatedEvent createdEvent = new UserCreatedEvent(userId, name, email, Instant.now());
        UserProfileUpdatedEvent profileUpdatedEvent = new UserProfileUpdatedEvent(
            userId, updatedName, email, Instant.now());
        ConsistencyPointsAwardedEvent pointsAwardedEvent = new ConsistencyPointsAwardedEvent(
            userId, points, points, "Achievement unlocked", Instant.now());

        AwardConsistencyPointsCommand command = new AwardConsistencyPointsCommand(
            userId, 25, "Bonus points");

        fixture.given(createdEvent, profileUpdatedEvent, pointsAwardedEvent)
                .when(command)
                .expectSuccessfulHandlerExecution()
                .expectEvents(new ConsistencyPointsAwardedEvent(
                    userId, 25, 125, "Bonus points", Instant.now()));
    }
}