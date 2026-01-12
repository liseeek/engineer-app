package com.example.medhub.event;

public record WorkerRegisteredEvent(
        String performedBy,
        Long workerId,
        String workerEmail,
        Long locationId
) {}
