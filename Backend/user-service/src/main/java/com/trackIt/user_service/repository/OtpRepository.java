package com.trackIt.user_service.repository;

import com.trackIt.user_service.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findByEmailAndOtpAndIsVerifiedFalse(String email, String otp);

    boolean existsByEmailAndIsVerifiedTrue(String email);

    void deleteByEmail(String email);

    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}