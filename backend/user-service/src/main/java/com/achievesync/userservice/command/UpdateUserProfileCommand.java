package com.achievesync.userservice.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class UpdateUserProfileCommand {
    @TargetAggregateIdentifier
    private final String userId;
    private final String name;
    private final String email;

    public UpdateUserProfileCommand(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}