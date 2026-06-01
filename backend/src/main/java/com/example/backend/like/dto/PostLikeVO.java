package com.example.backend.like.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PostLikeVO {
    private Long postId;
    private String username;
    private LocalDateTime createdAt;
}
