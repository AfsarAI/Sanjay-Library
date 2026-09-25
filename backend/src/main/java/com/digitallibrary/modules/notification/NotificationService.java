package com.digitallibrary.modules.notification;

import com.digitallibrary.modules.notification.adapter.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final List<NotificationSender> notificationSenders;

    public NotificationService(
            NotificationRepository notificationRepository,
            List<NotificationSender> notificationSenders) {
        this.notificationRepository = notificationRepository;
        this.notificationSenders = notificationSenders;
    }

    @Transactional
    public Notification sendNotification(Long libraryId, Long userId, String title, String message, String type) {
        return sendNotification(libraryId, userId, null, title, message, type);
    }

    @Transactional
    public Notification sendNotification(Long libraryId, Long userId, String recipientTarget, String title, String message, String type) {
        Notification notification = new Notification(libraryId, userId, title, message, type, "IN_APP");
        notification = notificationRepository.save(notification);
        log.info("Dispatched in-app notification [{}] to user {}: {}", type, userId, title);

        // Dispatch across external channels (FCM, Email, WhatsApp)
        for (NotificationSender sender : notificationSenders) {
            try {
                sender.send(userId, recipientTarget, title, message);
            } catch (Exception e) {
                log.warn("Failed to dispatch via sender {}: {}", sender.getChannel(), e.getMessage());
            }
        }

        return notification;
    }

    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }
}
