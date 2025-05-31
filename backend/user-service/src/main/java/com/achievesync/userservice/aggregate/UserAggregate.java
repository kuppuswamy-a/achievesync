package com.achievesync.userservice.aggregate;

import com.achievesync.userservice.command.AwardConsistencyPointsCommand;
import com.achievesync.userservice.command.CreateUserCommand;
import com.achievesync.userservice.command.UpdateUserProfileCommand;
import com.achievesync.userservice.event.ConsistencyPointsAwardedEvent;
import com.achievesync.userservice.event.UserCreatedEvent;
import com.achievesync.userservice.event.UserProfileUpdatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;

@Aggregate
public class UserAggregate {
    
    @AggregateIdentifier
    private String userId;
    private String name;
    private String email;
    private String passwordHash;
    private int totalConsistencyPoints;
    private Instant createdAt;
    private Instant updatedAt;

    public UserAggregate() {
        // Required by Axon
    }

    @CommandHandler
    public UserAggregate(CreateUserCommand command) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(command.getPassword());
        
        AggregateLifecycle.apply(new UserCreatedEvent(
            command.getUserId(),
            command.getName(),
            command.getEmail(),
            Instant.now()
        ));
    }

    @CommandHandler
    public void handle(UpdateUserProfileCommand command) {
        AggregateLifecycle.apply(new UserProfileUpdatedEvent(
            command.getUserId(),
            command.getName(),
            command.getEmail(),
            Instant.now()
        ));
    }

    @CommandHandler
    public void handle(AwardConsistencyPointsCommand command) {
        int newTotal = this.totalConsistencyPoints + command.getPoints();
        
        AggregateLifecycle.apply(new ConsistencyPointsAwardedEvent(
            command.getUserId(),
            command.getPoints(),
            newTotal,
            command.getReason(),
            Instant.now()
        ));
    }

    @EventSourcingHandler
    public void on(UserCreatedEvent event) {
        this.userId = event.getUserId();
        this.name = event.getName();
        this.email = event.getEmail();
        this.totalConsistencyPoints = 0;
        this.createdAt = event.getCreatedAt();
        this.updatedAt = event.getCreatedAt();
    }

    @EventSourcingHandler
    public void on(UserProfileUpdatedEvent event) {
        this.name = event.getName();
        this.email = event.getEmail();
        this.updatedAt = event.getUpdatedAt();
    }

    @EventSourcingHandler
    public void on(ConsistencyPointsAwardedEvent event) {
        this.totalConsistencyPoints = event.getTotalPoints();
    }

    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getTotalConsistencyPoints() { return totalConsistencyPoints; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}