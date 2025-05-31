package com.achievesync.goalservice.query;

public class FindGoalsByUserQuery {
    private final String userId;

    public FindGoalsByUserQuery(String userId) {
        this.userId = userId;
    }

    public String getUserId() { return userId; }
}