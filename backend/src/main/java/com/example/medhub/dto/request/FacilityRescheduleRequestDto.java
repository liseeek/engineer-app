package com.example.medhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FacilityRescheduleRequestDto(
        @NotNull Long newSlotId,
        @NotBlank @Size(max = 500) String reason) {
}
