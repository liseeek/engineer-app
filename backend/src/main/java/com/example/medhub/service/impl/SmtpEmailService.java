package com.example.medhub.service.impl;

import com.example.medhub.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final String frontendBaseUrl;

    @Override
    public void sendInvitationEmail(String to, String role, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("You have been invited to MedHub as " + role);

            String registrationUrl = frontendBaseUrl + "/register-invitation/" + token;
            String emailBody = buildEmailBody(role, registrationUrl);

            helper.setText(emailBody, true);

            mailSender.send(message);
            log.info("Invitation email sent successfully to {} via SMTP", to);
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send invitation email", e);
        }
    }

    private String buildEmailBody(String role, String registrationUrl) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Welcome to MedHub</h2>
                    <p>Hello,</p>
                    <p>You have been invited to join the MedHub platform as a <strong>%s</strong>.</p>
                    <p>Please click the link below to complete your registration:</p>
                    <p style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">
                            Complete Registration
                        </a>
                    </p>
                    <p style="font-size: 12px; color: #7f8c8d;">
                        Or copy and paste this link into your browser:<br>
                        <span style="word-break: break-all;">%s</span>
                    </p>
                    <p style="font-size: 12px; color: #7f8c8d;">
                        This link will expire in 24 hours.
                    </p>
                    <hr style="border: none; border-top: 1px solid #ecf0f1; margin: 30px 0;">
                    <p style="font-size: 12px; color: #95a5a6;">
                        If you did not expect this invitation, please ignore this email.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(role, registrationUrl, registrationUrl);
    }
}
