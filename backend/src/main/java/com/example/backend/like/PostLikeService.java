package com.example.backend.like;

public interface PostLikeService {

    boolean isLikedBy(Long postId, String username);

    boolean like(Long postId, String username);

    boolean unlike(Long postId, String username);
}
