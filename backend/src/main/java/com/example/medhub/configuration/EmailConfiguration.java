package com.example.medhub.configuration;

import com.example.medhub.service.EmailService;
import com.example.medhub.service.impl.ConsoleEmailService;
import com.example.medhub.service.impl.SmtpEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class EmailConfiguration {

    @Bean
    @Conditional(MailHostNonEmptyCondition.class)
    public EmailService smtpEmailService(
            JavaMailSender mailSender,
            @Value("${medhub.invitations.from-email}") String fromEmail,
            @Value("${medhub.invitations.frontend-base-url}") String frontendBaseUrl) {
        return new SmtpEmailService(mailSender, fromEmail, frontendBaseUrl);
    }

    @Bean
    @ConditionalOnMissingBean(EmailService.class)
    public EmailService consoleEmailService(
            @Value("${medhub.invitations.frontend-base-url:http://localhost:3000}") String frontendBaseUrl) {
        return new ConsoleEmailService(frontendBaseUrl);
    }
}
