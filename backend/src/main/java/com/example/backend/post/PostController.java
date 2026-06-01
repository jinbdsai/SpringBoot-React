package com.example.backend.post;

import com.example.backend.auth.SessionUser;
import com.example.backend.post.dto.PostRequest;
import com.example.backend.post.dto.PostResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<PostResponse> list(
            @RequestParam(required = false) PostCategory category,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false) String keyword,
            HttpSession session
    ) {
        return postService.findAll(category, sort, keyword, currentUsernameOrNull(session));
    }

    @GetMapping("/{id}")
    public PostResponse detail(@PathVariable Long id, HttpSession session) {
        return postService.findOne(id, currentUsernameOrNull(session));
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody PostRequest request, HttpSession session) {
        SessionUser user = requireLogin(session);
        PostResponse created = postService.create(user.getUsername(), request);
        return ResponseEntity.created(URI.create("/api/posts/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public PostResponse update(@PathVariable Long id, @Valid @RequestBody PostRequest request, HttpSession session) {
        SessionUser user = requireLogin(session);
        return postService.update(id, user.getUsername(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpSession session) {
        SessionUser user = requireLogin(session);
        postService.delete(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    private SessionUser requireLogin(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (user == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return user;
    }

    private String currentUsernameOrNull(HttpSession session) {
        SessionUser u = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        return u == null ? null : u.getUsername();
    }
}
