package com.example.backend.notification;

import com.example.backend.notification.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void notifyLike(String recipient, String fromUser, Long postId, String postTitle) {
        if (recipient.equals(fromUser)) return;   // 자기 글에 자기가 좋아요는 알림 X
        String msg = String.format("%s 님이 \"%s\" 글을 좋아합니다.", fromUser, truncate(postTitle, 30));
        repository.save(new Notification(recipient, NotificationType.LIKE, msg, postId, fromUser));
        log.info("알림 생성(LIKE): to={}, from={}, postId={}", recipient, fromUser, postId);
    }

    @Transactional
    public void notifyComment(String recipient, String fromUser, Long postId, String postTitle) {
        if (recipient.equals(fromUser)) return;
        String msg = String.format("%s 님이 \"%s\" 글에 댓글을 남겼습니다.", fromUser, truncate(postTitle, 30));
        repository.save(new Notification(recipient, NotificationType.COMMENT, msg, postId, fromUser));
        log.info("알림 생성(COMMENT): to={}, from={}, postId={}", recipient, fromUser, postId);
    }

    public List<NotificationResponse> findMine(String recipient) {
        return repository.findByRecipientOrderByIdDesc(recipient).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public long countUnread(String recipient) {
        return repository.countByRecipientAndIsReadFalse(recipient);
    }

    @Transactional
    public void markRead(Long id, String recipient) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));
        if (!n.getRecipient().equals(recipient)) {
            throw new SecurityException("본인 알림만 처리할 수 있습니다.");
        }
        n.markRead();
    }

    @Transactional
    public int markAllRead(String recipient) {
        return repository.markAllReadByRecipient(recipient);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
