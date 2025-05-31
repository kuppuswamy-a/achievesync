package com.achievesync.userservice.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class CreateUserCommand {
    @TargetAggregateIdentifier
    private final String userId;
    private final String name;
    private final String email;
    private final String password;

    public CreateUserCommand(String userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}