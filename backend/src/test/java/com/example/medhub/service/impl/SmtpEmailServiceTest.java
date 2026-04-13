package com.example.medhub.service.impl;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private SmtpEmailService emailService;
    private final String fromEmail = "noreply@test.com";
    private final String frontendBaseUrl = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        emailService = new SmtpEmailService(mailSender, fromEmail, frontendBaseUrl);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void shouldSendEmail_WhenInvitationIsValid() throws Exception {
        String to = "doctor@test.com";
        String role = "DOCTOR";
        String token = "test-token-123";

        emailService.sendInvitationEmail(to, role, token);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldThrowException_WhenEmailSendingFails() {
        String to = "doctor@test.com";
        String role = "DOCTOR";
        String token = "test-token-123";

        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> emailService.sendInvitationEmail(to, role, token));

        assertTrue(exception.getMessage().contains("Failed to send invitation email"));
        verify(mailSender).send(mimeMessage);
    }
}
