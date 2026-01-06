package com.trackIt.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationSender {

    public void send(String to, String subject, String body) {

        // Later: Email / Push / SMS
        log.info("""
                Sending Notification
                To: {}
                Subject: {}
                Body:
                {}
                """, to, subject, body);
    }
}
