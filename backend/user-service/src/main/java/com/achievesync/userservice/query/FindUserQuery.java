package com.achievesync.userservice.query;

public class FindUserQuery {
    private final String userId;

    public FindUserQuery(String userId) {
        this.userId = userId;
    }

    public String getUserId() { return userId; }
}