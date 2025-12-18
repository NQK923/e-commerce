package com.learnfirebase.ecommerce.identity.application.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;
import java.util.HexFormat;

import com.learnfirebase.ecommerce.identity.application.dto.OtpChallengeDto;
import com.learnfirebase.ecommerce.identity.application.port.out.OtpRepository;
import com.learnfirebase.ecommerce.identity.application.port.out.OtpSender;
import com.learnfirebase.ecommerce.identity.domain.exception.IdentityDomainException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OtpService {
    private static final Duration EXPIRY = Duration.ofMinutes(5);
    private static final int MAX_ATTEMPTS = 5;
    private final SecureRandom secureRandom = new SecureRandom();

    private final OtpRepository otpRepository;
    private final OtpSender otpSender;

    public OtpChallengeDto issue(String email, String userId) {
        String code = generateCode();
        String hash = hash(code);
        Instant expiresAt = Instant.now().plus(EXPIRY);
        String id = otpRepository.save(email, userId, hash, expiresAt);
        otpSender.sendOtp(email, code);
        return OtpChallengeDto.builder()
            .id(id)
            .email(email)
            .expiresAt(expiresAt)
            .build();
    }

    public void validate(String challengeId, String code, String expectedUserId, String expectedEmail) {
        var record = otpRepository.findById(challengeId)
            .orElseThrow(() -> new IdentityDomainException("OTP challenge not found"));
        if (record.used()) {
            throw new IdentityDomainException("OTP already used");
        }
        if (record.expiresAt().isBefore(Instant.now())) {
            throw new IdentityDomainException("OTP expired");
        }
        if (record.attempts() >= MAX_ATTEMPTS) {
            throw new IdentityDomainException("Too many OTP attempts");
        }
        if (expectedUserId != null && record.userId() != null && !expectedUserId.equals(record.userId())) {
            throw new IdentityDomainException("OTP does not match user");
        }
        if (expectedEmail != null && record.email() != null && !expectedEmail.equalsIgnoreCase(record.email())) {
            throw new IdentityDomainException("OTP does not match email");
        }
        String hash = hash(code);
        if (!hash.equals(record.codeHash())) {
            otpRepository.incrementAttempts(record.id());
            throw new IdentityDomainException("Invalid OTP code");
        }
        otpRepository.markUsed(record.id());
    }

    private String generateCode() {
        int value = secureRandom.nextInt(900000) + 100000;
        return Integer.toString(value);
    }

    private String hash(String code) {
        return hashInternal(code);
    }

    public String hashRaw(String token) {
        return hashInternal(token);
    }

    private String hashInternal(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
