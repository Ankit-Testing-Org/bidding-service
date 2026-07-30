package com.evatech.bidplatform.user.repository;

import com.evatech.bidplatform.user.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, String> {
    Optional<OtpToken> findByIdentifierAndOtpAndUsedFalse(String identifier, String otp);
}
