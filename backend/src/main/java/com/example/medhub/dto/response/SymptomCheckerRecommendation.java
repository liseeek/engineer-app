package com.example.medhub.dto.response;

import java.util.List;

/**
 * Structured output target for the symptom checker response.
 */
public record SymptomCheckerRecommendation(List<Entry> recommendations) {

    public record Entry(String specializationName, String confidence, String reasoning) {}
}
