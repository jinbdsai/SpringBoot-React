package com.example.backend.auth;

import com.example.backend.user.User;

import java.io.Serializable;

public class SessionUser implements Serializable {

    public static final String SESSION_KEY = "LOGIN_USER";

    private final Long id;
    private final String username;

    public SessionUser(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public static SessionUser from(User user) {
        return new SessionUser(user.getId(), user.getUsername());
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
