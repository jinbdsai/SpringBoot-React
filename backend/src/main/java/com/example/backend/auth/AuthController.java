package com.example.backend.auth;

import com.example.backend.user.User;
import com.example.backend.user.UserService;
import com.example.backend.user.dto.LoginRequest;
import com.example.backend.user.dto.RegisterRequest;
import com.example.backend.user.dto.UserResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody RegisterRequest request, HttpSession session) {
        User user = userService.register(request.getUsername(), request.getPassword());
        session.setAttribute(SessionUser.SESSION_KEY, SessionUser.from(user));
        return UserResponse.from(user);
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request, HttpSession session) {
        User user = userService.authenticate(request.getUsername(), request.getPassword());
        session.setAttribute(SessionUser.SESSION_KEY, SessionUser.from(user));
        return UserResponse.from(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(HttpSession session) {
        SessionUser sessionUser = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (sessionUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new UserResponse(sessionUser.getId(), sessionUser.getUsername()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(e.getMessage());
    }
}
