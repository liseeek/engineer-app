package com.example.medhub.configuration.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SecurityEnvironmentValidator implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(SecurityEnvironmentValidator.class);

    @Value("${jwt.secret.key:}")
    private String jwtSecretKey;

    @Value("${medhub.security.encryption.key:}")
    private String encryptionKey;

    @Value("${medhub.security.hashing.salt:}")
    private String hashingSalt;

    @Override
    public void afterPropertiesSet() {
        log.info("Validating security environment variables...");

        validateSecret("JWT_SECRET_KEY", jwtSecretKey, 64, "REPLACE_WITH_YOUR_BASE64_SECRET");
        validateSecret("ENCRYPTION_KEY", encryptionKey, 32, "REPLACE_WITH_32_CHAR_AES_KEY");
        validateSecret("HASHING_SALT", hashingSalt, 16, "REPLACE_WITH_SECURE_SALT");

        log.info("Security environment validation successful.");
    }

    private void validateSecret(String name, String value, int minLength, String placeholder) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is missing! Please provide it in your environment/.env file.");
        }
        if (value.equals(placeholder)) {
            throw new IllegalStateException(name + " is still set to the default placeholder value! Change it for security.");
        }
        if (value.length() < minLength) {
            throw new IllegalStateException(name + " is too short! Minimum required length is " + minLength + " characters.");
        }
    }
}
