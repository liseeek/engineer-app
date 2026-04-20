package com.example.medhub.dto.response;

import java.util.List;

/**
 * Structured output target for the Gemini response.
 * Spring AI maps the JSON output directly onto this record.
 */
public record AiRecommendation(List<Entry> recommendations) {

    public record Entry(String specializationName, String confidence, String reasoning) {}
}
