package com.example.backend.like;

import com.example.backend.auth.SessionUser;
import com.example.backend.like.dto.PostLikeResponseDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/like")
public class PostLikeController {

    private final PostLikeService likeService;

    public PostLikeController(PostLikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    public PostLikeResponseDTO like(@PathVariable Long postId, HttpSession session) {
        SessionUser u = requireLogin(session);
        boolean added = likeService.like(postId, u.getUsername());
        return new PostLikeResponseDTO(true, added);
    }

    @DeleteMapping
    public PostLikeResponseDTO unlike(@PathVariable Long postId, HttpSession session) {
        SessionUser u = requireLogin(session);
        boolean removed = likeService.unlike(postId, u.getUsername());
        return new PostLikeResponseDTO(false, removed);
    }

    private SessionUser requireLogin(HttpSession session) {
        SessionUser u = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (u == null) throw new SecurityException("로그인이 필요합니다.");
        return u;
    }
}
