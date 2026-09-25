package com.digitallibrary.modules.notification.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class FcmNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(FcmNotificationSender.class);

    private final boolean enabled;
    private final String serverKey;
    private final HttpClient httpClient;

    public FcmNotificationSender(
            @Value("${app.notifications.fcm.enabled:false}") boolean enabled,
            @Value("${app.notifications.fcm.server-key:}") String serverKey) {
        this.enabled = enabled;
        this.serverKey = serverKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public String getChannel() {
        return "FCM_PUSH";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void send(Long userId, String recipient, String title, String message) {
        if (!enabled) {
            log.info("[SIMULATED FCM] Push notification to user {} (token: {}): {} - {}", 
                    userId, recipient != null ? recipient : "N/A", title, message);
            return;
        }

        try {
            if (recipient == null || recipient.isBlank()) {
                log.warn("FCM push aborted: No device token provided for user {}", userId);
                return;
            }

            String jsonPayload = String.format(
                    "{\"to\":\"%s\",\"notification\":{\"title\":\"%s\",\"body\":\"%s\"}}",
                    recipient, escapeJson(title), escapeJson(message));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://fcm.googleapis.com/fcm/send"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "key=" + serverKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("FCM push sent successfully to user {}: status {}", userId, response.statusCode());
            } else {
                log.warn("FCM push failed for user {}: HTTP {} - {}", userId, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Error dispatching FCM push notification to user {}", userId, e);
        }
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
