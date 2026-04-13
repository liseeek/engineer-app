package com.example.medhub.configuration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * {@code @ConditionalOnProperty(spring.mail.host)} is true even when the value is an empty string
 * (e.g. {@code MAIL_HOST=} in .env), which wrongly selects SMTP instead of the console fallback.
 */
public class MailHostNonEmptyCondition implements Condition {

    private static final String PROP = "spring.mail.host";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String host = context.getEnvironment().getProperty(PROP);
        return StringUtils.hasText(host);
    }
}
