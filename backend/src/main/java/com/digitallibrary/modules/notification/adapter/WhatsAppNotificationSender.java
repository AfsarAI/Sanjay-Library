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
public class WhatsAppNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificationSender.class);

    private final boolean enabled;
    private final String apiUrl;
    private final String phoneNumberId;
    private final String accessToken;
    private final HttpClient httpClient;

    public WhatsAppNotificationSender(
            @Value("${app.notifications.whatsapp.enabled:false}") boolean enabled,
            @Value("${app.notifications.whatsapp.api-url:https://graph.facebook.com/v18.0}") String apiUrl,
            @Value("${app.notifications.whatsapp.phone-number-id:}") String phoneNumberId,
            @Value("${app.notifications.whatsapp.access-token:}") String accessToken) {
        this.enabled = enabled;
        this.apiUrl = apiUrl;
        this.phoneNumberId = phoneNumberId;
        this.accessToken = accessToken;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Override
    public String getChannel() {
        return "WHATSAPP";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void send(Long userId, String recipientPhone, String title, String message) {
        if (!enabled) {
            log.info("[SIMULATED WHATSAPP] To user {} (phone: {}): Title: '{}', Body: '{}'", 
                    userId, recipientPhone != null ? recipientPhone : "N/A", title, message);
            return;
        }

        try {
            if (recipientPhone == null || recipientPhone.isBlank()) {
                log.warn("WhatsApp dispatch aborted: No phone number provided for user {}", userId);
                return;
            }

            // Ensure proper country code prefix for WhatsApp (India +91 default)
            String formattedPhone = recipientPhone.trim();
            if (formattedPhone.length() == 10 && !formattedPhone.startsWith("+")) {
                formattedPhone = "91" + formattedPhone;
            } else if (formattedPhone.startsWith("+")) {
                formattedPhone = formattedPhone.substring(1);
            }

            String endpoint = String.format("%s/%s/messages", apiUrl, phoneNumberId);
            String fullBodyText = title != null && !title.isBlank() 
                    ? String.format("*%s*\n%s", title, message)
                    : message;

            String jsonPayload = String.format(
                    "{\"messaging_product\":\"whatsapp\",\"recipient_type\":\"individual\",\"to\":\"%s\",\"type\":\"text\",\"text\":{\"body\":\"%s\"}}",
                    formattedPhone, escapeJson(fullBodyText));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("WhatsApp notification sent successfully to user {} ({}): HTTP {}", 
                        userId, formattedPhone, response.statusCode());
            } else {
                log.warn("WhatsApp notification failed for user {} ({}): HTTP {} - {}", 
                        userId, formattedPhone, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Error dispatching WhatsApp notification to user {}", userId, e);
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
