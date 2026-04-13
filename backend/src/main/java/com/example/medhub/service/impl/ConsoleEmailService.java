package com.example.medhub.service.impl;

import com.example.medhub.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ConsoleEmailService implements EmailService {

    private final String frontendBaseUrl;

    @Override
    public void sendInvitationEmail(String to, String role, String token) {
        log.info("================ EMAIL SENT (CONSOLE MODE) ================");
        log.info("TO: {}", to);
        log.info("SUBJECT: You have been invited to MedHub as {}", role);
        log.info("BODY:");
        log.info("Hello,");
        log.info("You have been invited to join the MedHub platform as a {}.", role);
        log.info("Please click the link below to complete your registration:");
        log.info("{}/register-invitation/{}", frontendBaseUrl, token);
        log.info("This link will expire in 24 hours.");
        log.info("============================================");
    }
}
