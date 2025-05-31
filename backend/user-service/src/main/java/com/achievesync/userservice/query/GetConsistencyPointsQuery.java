package com.achievesync.userservice.query;

public class GetConsistencyPointsQuery {
    private final String userId;

    public GetConsistencyPointsQuery(String userId) {
        this.userId = userId;
    }

    public String getUserId() { return userId; }
}