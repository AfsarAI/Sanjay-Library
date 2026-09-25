package com.digitallibrary.modules.notification.adapter;

public interface NotificationSender {
    String getChannel();
    boolean isEnabled();
    void send(Long userId, String recipient, String title, String message);
}
