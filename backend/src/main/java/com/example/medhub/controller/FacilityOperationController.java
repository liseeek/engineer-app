package com.example.medhub.controller;

import com.example.medhub.service.FacilityOperationService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/facility")
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

}
