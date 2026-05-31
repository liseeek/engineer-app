package com.example.medhub.dto.response;

import java.time.LocalDateTime;

public record VisitNoteDto(
        Long id,
        Long appointmentId,
        Long doctorId,
        String diagnosis,
        String prescription,
        String notes,
        LocalDateTime createdAt
) {}
