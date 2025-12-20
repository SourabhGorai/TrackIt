package com.trackIt.user_service.scheduler;

import com.trackIt.user_service.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OtpCleanupScheduler {

    private final OtpService otpService;

    /**
     * Cleanup expired OTPs every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredOtps() {
        log.info("Running OTP cleanup job");
        otpService.cleanupExpiredOtps();
    }
}