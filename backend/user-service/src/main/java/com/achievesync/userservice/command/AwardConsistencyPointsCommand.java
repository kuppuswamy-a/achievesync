package com.achievesync.userservice.command;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class AwardConsistencyPointsCommand {
    @TargetAggregateIdentifier
    private final String userId;
    private final int points;
    private final String reason;

    public AwardConsistencyPointsCommand(String userId, int points, String reason) {
        this.userId = userId;
        this.points = points;
        this.reason = reason;
    }

    public String getUserId() { return userId; }
    public int getPoints() { return points; }
    public String getReason() { return reason; }
}