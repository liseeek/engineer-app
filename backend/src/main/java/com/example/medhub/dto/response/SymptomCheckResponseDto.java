package com.example.medhub.dto.response;

import java.util.List;

public record SymptomCheckResponseDto(
        List<SpecializationRecommendation> recommendations,
        String disclaimer
) {
    public static final String DISCLAIMER_TEXT =
            "This tool provides general guidance only and is not a substitute for professional medical advice. "
                    + "Always consult a qualified healthcare provider for diagnosis and treatment.";
}
