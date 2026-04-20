package com.example.medhub.dto.request;

import jakarta.validation.constraints.NotNull;

public record RescheduleRequestDto(@NotNull Long newSlotId) {
}
