package com.achievesync.userservice.controller;

import com.achievesync.userservice.command.AwardConsistencyPointsCommand;
import com.achievesync.userservice.command.CreateUserCommand;
import com.achievesync.userservice.command.UpdateUserProfileCommand;
import com.achievesync.userservice.projection.ConsistencyPointsProjection;
import com.achievesync.userservice.projection.UserProjection;
import com.achievesync.userservice.query.FindUserQuery;
import com.achievesync.userservice.query.GetConsistencyPointsQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public UserController(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<String>> createUser(@RequestBody CreateUserRequest request) {
        String userId = UUID.randomUUID().toString();
        CreateUserCommand command = new CreateUserCommand(
            userId, 
            request.getName(), 
            request.getEmail(), 
            request.getPassword()
        );
        
        return commandGateway.send(command)
            .thenApply(result -> ResponseEntity.status(HttpStatus.CREATED).body(userId));
    }

    @GetMapping("/{userId}")
    public CompletableFuture<ResponseEntity<UserProjection>> getUser(@PathVariable String userId) {
        return queryGateway.query(new FindUserQuery(userId), UserProjection.class)
            .thenApply(user -> user != null ? 
                ResponseEntity.ok(user) : 
                ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}")
    public CompletableFuture<ResponseEntity<Void>> updateUser(@PathVariable String userId, 
                                                            @RequestBody UpdateUserRequest request) {
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
            userId, 
            request.getName(), 
            request.getEmail()
        );
        
        return commandGateway.send(command)
            .thenApply(result -> ResponseEntity.ok().build());
    }

    @PostMapping("/{userId}/points")
    public CompletableFuture<ResponseEntity<Void>> awardPoints(@PathVariable String userId, 
                                                             @RequestBody AwardPointsRequest request) {
        AwardConsistencyPointsCommand command = new AwardConsistencyPointsCommand(
            userId, 
            request.getPoints(), 
            request.getReason()
        );
        
        return commandGateway.send(command)
            .thenApply(result -> ResponseEntity.ok().build());
    }

    @GetMapping("/{userId}/points")
    public CompletableFuture<ResponseEntity<ConsistencyPointsProjection>> getPoints(@PathVariable String userId) {
        return queryGateway.query(new GetConsistencyPointsQuery(userId), ConsistencyPointsProjection.class)
            .thenApply(points -> points != null ? 
                ResponseEntity.ok(points) : 
                ResponseEntity.notFound().build());
    }

    // Request DTOs
    public static class CreateUserRequest {
        private String name;
        private String email;
        private String password;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class UpdateUserRequest {
        private String name;
        private String email;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class AwardPointsRequest {
        private int points;
        private String reason;

        public int getPoints() { return points; }
        public void setPoints(int points) { this.points = points; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}