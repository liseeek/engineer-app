package com.example.medhub.dto.response;

public record SpecializationRecommendation(
        Long specializationId,
        String specializationName,
        String confidence,
        String reasoning
) {}
