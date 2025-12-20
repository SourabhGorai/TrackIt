package com.trackIt.user_service.service;

import com.trackIt.user_service.exception.InvalidOtpException;
import com.trackIt.user_service.exception.OtpExpiredException;
import com.trackIt.user_service.model.OtpVerification;
import com.trackIt.user_service.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate and send OTP to email
     */
    @Transactional
    public void generateAndSendOtp(String email) {
        log.info("Generating OTP for email: {}", email);

        // Invalidate any existing OTPs for this email
        otpRepository.deleteByEmail(email);

        // Generate 6-digit OTP
        String otp = String.format("%06d", random.nextInt(999999));

        // Save OTP
        OtpVerification otpVerification = OtpVerification.builder()
                .email(email)
                .otp(otp)
                .isVerified(false)
                .build();

        otpRepository.save(otpVerification);

        // Send email
        sendOtpEmail(email, otp);

        log.info("OTP sent successfully to: {}", email);
    }

    /**
     * Verify OTP
     */
    @Transactional
    public void verifyOtp(String email, String otp) {
        log.info("Verifying OTP for email: {}", email);

        OtpVerification verification = otpRepository.findByEmailAndOtpAndIsVerifiedFalse(email, otp)
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP provided"));

        if (verification.isExpired()) {
            log.error("OTP expired for email: {}", email);
            throw new OtpExpiredException("OTP has expired. Please request a new one.");
        }

        verification.setIsVerified(true);
        otpRepository.save(verification);

        log.info("OTP verified successfully for email: {}", email);
    }

    /**
     * Check if email is verified
     */
    public boolean isEmailVerified(String email) {
        return otpRepository.existsByEmailAndIsVerifiedTrue(email);
    }

    /**
     * Send OTP email
     */
    private void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("TrackIT - Email Verification OTP");
            message.setText(String.format(
                    "Your OTP for email verification is: %s\n\n" +
                            "This OTP is valid for 10 minutes.\n\n" +
                            "If you did not request this, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "TrackIT Team",
                    otp
            ));

            mailSender.send(message);
            log.info("OTP email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
    }

    /**
     * Cleanup expired OTPs (can be scheduled)
     */
    @Transactional
    public void cleanupExpiredOtps() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        otpRepository.deleteByCreatedAtBefore(cutoff);
        log.info("Cleaned up expired OTPs");
    }
}