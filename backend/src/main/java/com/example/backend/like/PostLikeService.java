package com.example.backend.like;

import com.example.backend.post.Post;
import com.example.backend.post.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository likeRepository;
    private final PostRepository postRepository;

    public PostLikeService(PostLikeRepository likeRepository, PostRepository postRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }

    public boolean isLikedBy(Long postId, String username) {
        if (username == null) return false;
        return likeRepository.existsByPostIdAndUsername(postId, username);
    }

    @Transactional
    public boolean like(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        if (likeRepository.existsByPostIdAndUsername(postId, username)) {
            return false;   // 이미 좋아요됨
        }
        likeRepository.save(new PostLike(postId, username));
        post.incrementLikeCount();
        return true;
    }

    @Transactional
    public boolean unlike(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        if (!likeRepository.existsByPostIdAndUsername(postId, username)) {
            return false;
        }
        likeRepository.deleteByPostIdAndUsername(postId, username);
        post.decrementLikeCount();
        return true;
    }
}
