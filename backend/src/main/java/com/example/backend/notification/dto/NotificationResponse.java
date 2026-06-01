package com.example.backend.notification.dto;

import com.example.backend.notification.Notification;
import com.example.backend.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String message;
    private Long postId;
    private String fromUser;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType(), n.getMessage(),
                n.getPostId(), n.getFromUser(),
                n.getIsRead(), n.getCreatedAt()
        );
    }
}
