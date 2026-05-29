package com.example.backend.comment;

import com.example.backend.auth.SessionUser;
import com.example.backend.comment.dto.CommentRequest;
import com.example.backend.comment.dto.CommentResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/api/posts/{postId}/comments")
    public List<CommentResponse> list(@PathVariable Long postId) {
        return commentService.findByPost(postId);
    }

    @PostMapping("/api/posts/{postId}/comments")
    public CommentResponse create(@PathVariable Long postId, @RequestBody CommentRequest request, HttpSession session) {
        SessionUser user = requireLogin(session);
        return commentService.create(postId, user.getUsername(), request);
    }

    @PutMapping("/api/comments/{commentId}")
    public CommentResponse update(@PathVariable Long commentId, @RequestBody CommentRequest request, HttpSession session) {
        SessionUser user = requireLogin(session);
        return commentService.update(commentId, user.getUsername(), request);
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long commentId, HttpSession session) {
        SessionUser user = requireLogin(session);
        commentService.delete(commentId, user.getUsername());
        return ResponseEntity.noContent().build();
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
