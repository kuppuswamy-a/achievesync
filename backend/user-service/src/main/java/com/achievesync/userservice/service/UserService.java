package com.achievesync.userservice.service;

import com.achievesync.userservice.command.CreateUserCommand;
import com.achievesync.userservice.command.UpdateUserProfileCommand;
import com.achievesync.userservice.command.AwardConsistencyPointsCommand;
import com.achievesync.userservice.projection.UserProjection;
import com.achievesync.userservice.projection.ConsistencyPointsProjection;
import com.achievesync.userservice.query.FindUserQuery;
import com.achievesync.userservice.query.FindUserByEmailQuery;
import com.achievesync.userservice.query.GetConsistencyPointsQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private QueryGateway queryGateway;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public CompletableFuture<String> createUser(String name, String email, String password) {
        String userId = UUID.randomUUID().toString();
        String hashedPassword = passwordEncoder.encode(password);
        CreateUserCommand command = new CreateUserCommand(userId, name, email, hashedPassword);
        
        return commandGateway.send(command).thenApply(result -> userId);
    }

    public CompletableFuture<Void> updateUserProfile(String userId, String name, String email) {
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(userId, name, email);
        return commandGateway.send(command);
    }

    public CompletableFuture<Void> awardConsistencyPoints(String userId, int points, String reason) {
        AwardConsistencyPointsCommand command = new AwardConsistencyPointsCommand(userId, points, reason);
        return commandGateway.send(command);
    }

    public CompletableFuture<UserProjection> getUser(String userId) {
        return queryGateway.query(new FindUserQuery(userId), UserProjection.class);
    }

    public CompletableFuture<UserProjection> getUserByEmail(String email) {
        return queryGateway.query(new FindUserByEmailQuery(email), UserProjection.class);
    }

    public CompletableFuture<ConsistencyPointsProjection> getConsistencyPoints(String userId) {
        return queryGateway.query(new GetConsistencyPointsQuery(userId), ConsistencyPointsProjection.class);
    }

    public boolean validatePassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}