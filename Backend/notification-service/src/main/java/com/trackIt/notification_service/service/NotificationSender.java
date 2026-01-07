package com.trackIt.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSender {

    private final JavaMailSender mailSender;

    @Value("${app.notification.sender-email:noreply@trackit.com}")
    private String senderEmail;

    @Value("${app.notification.sender-name:TrackIt Support System}")
    private String senderName;

    @Value("${app.notification.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send email notification
     */
    public void send(String to, String subject, String body) {

        if (!emailEnabled) {
            log.info("📧 Email sending is disabled. Would have sent to: {}", to);
            logEmail(to, subject, body);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // false = plain text, true = HTML

            mailSender.send(message);

            log.info("✅ Email sent successfully to: {}", to);
            log.debug("Subject: {}", subject);

        } catch (MessagingException e) {
            log.error("❌ Failed to send email to: {}. Error: {}", to, e.getMessage(), e);
            // Don't throw exception - we don't want email failures to break the notification flow
        } catch (Exception e) {
            log.error("❌ Unexpected error sending email to: {}", to, e);
        }
    }

    /**
     * Send HTML email notification
     */
    public void sendHtml(String to, String subject, String htmlBody) {

        if (!emailEnabled) {
            log.info("📧 Email sending is disabled. Would have sent HTML email to: {}", to);
            logEmail(to, subject, htmlBody);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content

            mailSender.send(message);

            log.info("✅ HTML Email sent successfully to: {}", to);
            log.debug("Subject: {}", subject);

        } catch (MessagingException e) {
            log.error("❌ Failed to send HTML email to: {}. Error: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Unexpected error sending HTML email to: {}", to, e);
        }
    }

    /**
     * Log email details (for debugging or when email is disabled)
     */
    private void logEmail(String to, String subject, String body) {
        log.info("""
                
                ═══════════════════════════════════════════════════════
                📧 EMAIL NOTIFICATION (Not Sent - Logging Only)
                ═══════════════════════════════════════════════════════
                To: {}
                Subject: {}
                ───────────────────────────────────────────────────────
                Body:
                {}
                ═══════════════════════════════════════════════════════
                """, to, subject, body);
    }
}