package com.trackIt.user_service2.repository;

import com.trackIt.user_service2.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(String email);

    List<OtpVerification> findByEmailAndCreatedAtBefore(String email, LocalDateTime cutoffTime);

    void deleteByExpiresAtBefore(LocalDateTime now);
}