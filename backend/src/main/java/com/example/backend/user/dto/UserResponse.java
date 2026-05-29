package com.example.backend.user.dto;

import com.example.backend.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername());
    }
}
