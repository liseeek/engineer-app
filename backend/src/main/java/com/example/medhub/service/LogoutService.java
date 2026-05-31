package com.example.medhub.service;

import com.example.medhub.configuration.security.jwt.JwtService;
import com.example.medhub.entity.BlacklistedToken;
import com.example.medhub.repository.BlacklistedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            Date expirationDate = jwtService.extractExpiration(jwt);
            LocalDateTime expiresAt = expirationDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                    .token(jwt)
                    .expiresAt(expiresAt)
                    .build();

            blacklistedTokenRepository.save(blacklistedToken);
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void cleanUpExpiredTokens() {
        blacklistedTokenRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }
}
