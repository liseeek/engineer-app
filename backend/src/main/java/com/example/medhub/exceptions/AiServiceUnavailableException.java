package com.example.medhub.exceptions;

/**
 * Thrown when the external AI provider (e.g. Gemini) is unavailable, misconfigured, or rate-limited.
 * Mapped to HTTP 503 by {@link GlobalExceptionHandler}.
 */
public class AiServiceUnavailableException extends MedHubServiceException {

    public AiServiceUnavailableException(String message) {
        super(message);
    }
}
