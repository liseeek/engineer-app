package com.example.medhub.dto.request;

import com.example.medhub.enums.AgeRange;
import com.example.medhub.enums.Gender;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SymptomCheckRequestDto(
        @NotNull AgeRange ageRange,
        @NotNull Gender gender,
        @NotEmpty List<String> symptoms,
        @Size(max = 500) String additionalDescription
) {}
