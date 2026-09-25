package com.digitallibrary.modules.notification.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final boolean enabled;
    private final String fromAddress;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public EmailNotificationSender(
            @Value("${app.notifications.email.enabled:false}") boolean enabled,
            @Value("${app.notifications.email.from:noreply@sanjaylibrary.com}") String fromAddress,
            ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.enabled = enabled;
        this.fromAddress = fromAddress;
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    public String getChannel() {
        return "EMAIL";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void send(Long userId, String recipientEmail, String title, String message) {
        if (!enabled) {
            log.info("[SIMULATED EMAIL] To user {} (email: {}): Subject: '{}', Body: '{}'", 
                    userId, recipientEmail != null ? recipientEmail : "N/A", title, message);
            return;
        }

        try {
            if (recipientEmail == null || recipientEmail.isBlank()) {
                log.warn("Email dispatch skipped: No recipient email for user {}", userId);
                return;
            }

            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null) {
                log.warn("Email sender is enabled but JavaMailSender bean is not configured in Spring context.");
                return;
            }

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromAddress);
            mailMessage.setTo(recipientEmail);
            mailMessage.setSubject(title);
            mailMessage.setText(message);

            mailSender.send(mailMessage);
            log.info("Email sent successfully to user {} ({})", userId, recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send email to user {} ({})", userId, recipientEmail, e);
        }
    }
}
