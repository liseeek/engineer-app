package com.example.medhub.exceptions;

/**
 * Thrown when the external symptom checker provider is unavailable or misconfigured.
 * Mapped to HTTP 503 by {@link GlobalExceptionHandler}.
 */
public class SymptomCheckerUnavailableException extends MedHubServiceException {

    public SymptomCheckerUnavailableException(String message) {
        super(message);
    }
}
