package com.example.backend.user;

import com.example.backend.auth.SessionUser;
import com.example.backend.post.PostService;
import com.example.backend.post.dto.PostResponse;
import com.example.backend.user.dto.UserResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PostService postService;

    public UserController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @GetMapping("/{username}")
    public Map<String, Object> profile(@PathVariable String username, HttpSession session) {
        User user = userService.findByUsername(username);
        String me = currentUsernameOrNull(session);
        List<PostResponse> posts = postService.findByAuthor(username, me);
        long likeCountSum = posts.stream().mapToLong(p -> p.getLikeCount() == null ? 0 : p.getLikeCount()).sum();
        long viewCountSum = posts.stream().mapToLong(p -> p.getViewCount() == null ? 0 : p.getViewCount()).sum();
        return Map.of(
                "user", UserResponse.from(user),
                "postCount", posts.size(),
                "totalLikes", likeCountSum,
                "totalViews", viewCountSum,
                "joinedAt", user.getCreatedAt(),
                "posts", posts
        );
    }

    private String currentUsernameOrNull(HttpSession session) {
        SessionUser u = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        return u == null ? null : u.getUsername();
    }
}
