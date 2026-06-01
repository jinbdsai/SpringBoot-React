package com.example.backend.like.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeDTO {
    private Long postId;
    private String username;
    private LocalDateTime createdAt;

    public static PostLikeDTO from(PostLikeVO vo) {
        return new PostLikeDTO(vo.getPostId(), vo.getUsername(), vo.getCreatedAt());
    }
}
