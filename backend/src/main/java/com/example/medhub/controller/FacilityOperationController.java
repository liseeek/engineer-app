package com.example.medhub.controller;

import com.example.medhub.dto.request.FacilityRescheduleRequestDto;
import com.example.medhub.service.FacilityOperationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/facility")
@RequiredArgsConstructor
@PreAuthorize("hasRole('WORKER')")
public class FacilityOperationController {

    private final FacilityOperationService facilityOperationService;

    @PostMapping("/appointments/{id}/cancel")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable Long id,
            @RequestParam @NotBlank String reason) {
        facilityOperationService.cancelAppointment(id, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/appointments/{id}/reschedule")
    public ResponseEntity<Void> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody FacilityRescheduleRequestDto dto) {
        facilityOperationService.rescheduleAppointment(id, dto.newSlotId(), dto.reason());
        return ResponseEntity.noContent().build();
    }

}
