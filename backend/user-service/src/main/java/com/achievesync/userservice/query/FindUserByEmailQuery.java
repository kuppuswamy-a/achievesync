package com.achievesync.userservice.query;

public class FindUserByEmailQuery {
    private final String email;

    public FindUserByEmailQuery(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
}