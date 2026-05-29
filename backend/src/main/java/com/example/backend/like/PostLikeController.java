package com.example.backend.like;

import com.example.backend.auth.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/like")
public class PostLikeController {

    private final PostLikeService likeService;

    public PostLikeController(PostLikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    public Map<String, Object> like(@PathVariable Long postId, HttpSession session) {
        SessionUser u = requireLogin(session);
        boolean added = likeService.like(postId, u.getUsername());
        return Map.of("liked", true, "changed", added);
    }

    @DeleteMapping
    public Map<String, Object> unlike(@PathVariable Long postId, HttpSession session) {
        SessionUser u = requireLogin(session);
        boolean removed = likeService.unlike(postId, u.getUsername());
        return Map.of("liked", false, "changed", removed);
    }

    private SessionUser requireLogin(HttpSession session) {
        SessionUser u = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (u == null) throw new SecurityException("로그인이 필요합니다.");
        return u;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> bad(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> forbidden(SecurityException e) {
        return ResponseEntity.status(403).body(e.getMessage());
    }
}
