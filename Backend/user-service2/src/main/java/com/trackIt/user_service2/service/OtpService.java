package com.trackIt.user_service2.service;

import com.trackIt.user_service2.exception.InvalidOtpException;
import com.trackIt.user_service2.exception.OtpAttemptsExceededException;
import com.trackIt.user_service2.exception.OtpExpiredException;
import com.trackIt.user_service2.model.OtpVerification;
import com.trackIt.user_service2.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;
    private static final SecureRandom random = new SecureRandom();

    @Value("${app.otp.length}")
    private int otpLength;

    @Value("${app.otp.expiration-minutes}")
    private int expirationMinutes;

    @Value("${app.otp.max-attempts}")
    private int maxAttempts;

    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    @Transactional
    public void createAndSendOtp(String email, String name) {
        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);

        OtpVerification otpVerification = OtpVerification.builder()
                .email(email)
                .otp(otp)
                .expiresAt(expiresAt)
                .isUsed(false)
                .attempts(0)
                .build();

        otpRepository.save(otpVerification);

        emailService.sendOtpEmail(email, name, otp, expirationMinutes);
        log.info("OTP generated and sent to email: {}", email);
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        Optional<OtpVerification> otpOptional = otpRepository
                .findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(email);

        if (otpOptional.isEmpty()) {
            throw new InvalidOtpException("No valid OTP found for this email");
        }

        OtpVerification otpVerification = otpOptional.get();

        if (otpVerification.isExpired()) {
            throw new OtpExpiredException("OTP has expired. Please request a new one");
        }

        if (otpVerification.getAttempts() >= maxAttempts) {
            throw new OtpAttemptsExceededException("Maximum OTP verification attempts exceeded. Please request a new OTP");
        }

        if (!otpVerification.getOtp().equals(otp)) {
            otpVerification.incrementAttempts();
            otpRepository.save(otpVerification);

            int remainingAttempts = maxAttempts - otpVerification.getAttempts();
            throw new InvalidOtpException(
                    String.format("Invalid OTP. %d attempt(s) remaining", remainingAttempts)
            );
        }

        otpVerification.setIsUsed(true);
        otpRepository.save(otpVerification);
        log.info("OTP verified successfully for email: {}", email);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredOtps() {
        log.info("Starting cleanup of expired OTPs");
        otpRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Expired OTPs cleanup completed");
    }
}
