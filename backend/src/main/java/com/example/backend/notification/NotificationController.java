package com.example.backend.notification;

import com.example.backend.auth.SessionUser;
import com.example.backend.notification.dto.NotificationResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list(HttpSession session) {
        return notificationService.findMine(requireLogin(session).getUsername());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(HttpSession session) {
        SessionUser u = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        long count = (u == null) ? 0 : notificationService.countUnread(u.getUsername());
        return Map.of("count", count);
    }

    @PostMapping("/{id}/read")
    public Map<String, Object> markRead(@PathVariable Long id, HttpSession session) {
        SessionUser u = requireLogin(session);
        notificationService.markRead(id, u.getUsername());
        return Map.of("ok", true);
    }

    @PostMapping("/read-all")
    public Map<String, Object> markAllRead(HttpSession session) {
        SessionUser u = requireLogin(session);
        int updated = notificationService.markAllRead(u.getUsername());
        return Map.of("updated", updated);
    }

    private SessionUser requireLogin(HttpSession session) {
        SessionUser u = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        if (u == null) throw new SecurityException("로그인이 필요합니다.");
        return u;
    }
}
