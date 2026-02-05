package com.example.medhub.service;

public interface EmailService {
    void sendInvitationEmail(String to, String role, String token);
}
