package com.example.backend.like;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLike.PK> {

    boolean existsByPostIdAndUsername(Long postId, String username);

    void deleteByPostIdAndUsername(Long postId, String username);

    List<PostLike> findByUsername(String username);
}
