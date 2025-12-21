package com.trackIt.user_service2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String to, String name, String otp, int expirationMinutes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Email Verification - TrackIt");
            message.setText(buildOtpEmailBody(name, otp, expirationMinutes));

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send email. Please try again later");
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Welcome to TrackIt");
            message.setText(buildWelcomeEmailBody(name));

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String otp, int expirationMinutes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Password Reset - TrackIt");
            message.setText(buildPasswordResetEmailBody(name, otp, expirationMinutes));

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send email. Please try again later");
        }
    }

    private String buildOtpEmailBody(String name, String otp, int expirationMinutes) {
        return String.format("""
            Hello %s,
            
            Thank you for registering with TrackIt!
            
            Your verification code is: %s
            
            This code will expire in %d minutes.
            
            If you didn't request this code, please ignore this email.
            
            Best regards,
            TrackIt Team
            """, name, otp, expirationMinutes);
    }

    private String buildWelcomeEmailBody(String name) {
        return String.format("""
            Hello %s,
            
            Welcome to TrackIt! Your email has been successfully verified.
            
            You can now log in and start using our incident and SLA management system.
            
            Best regards,
            TrackIt Team
            """, name);
    }

    private String buildPasswordResetEmailBody(String name, String otp, int expirationMinutes) {
        return String.format("""
            Hello %s,
            
            We received a request to reset your password.
            
            Your password reset code is: %s
            
            This code will expire in %d minutes.
            
            If you didn't request a password reset, please ignore this email and your password will remain unchanged.
            
            Best regards,
            TrackIt Team
            """, name, otp, expirationMinutes);
    }
}