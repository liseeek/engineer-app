package com.example.medhub.controller;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.dto.DoctorLocationRequestDto;
import com.example.medhub.dto.VisitNoteRequestDto;
import com.example.medhub.entity.Doctor;
import com.example.medhub.service.DoctorLocationRequestService;
import com.example.medhub.service.DoctorWorkspaceService;
import com.example.medhub.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorWorkspaceController {

    private final DoctorWorkspaceService doctorWorkspaceService;
    private final DoctorLocationRequestService doctorLocationRequestService;
    private final SecurityService securityService;

    @GetMapping("/appointments")
    @Operation(summary = "Get current doctor's schedule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schedule retrieved."),
            @ApiResponse(responseCode = "403", description = "Forbidden.")
    })
    public ResponseEntity<List<AppointmentsDto>> getMySchedule() {
        return ResponseEntity.ok(doctorWorkspaceService.getMySchedule());
    }

    @PostMapping("/appointments/{id}/note")
    @Operation(summary = "Add visit note and conclude appointment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Visit concluded."),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "403", description = "Forbidden.")
    })
    public ResponseEntity<Void> concludeVisit(
            @PathVariable Long id,
            @RequestBody @Valid VisitNoteRequestDto noteDto) {
        doctorWorkspaceService.concludeVisit(id, noteDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/location-requests")
    @Operation(summary = "List pending facility assignment requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Requests listed."),
            @ApiResponse(responseCode = "403", description = "Forbidden.")
    })
    public ResponseEntity<List<DoctorLocationRequestDto>> listPendingLocationRequests() {
        Doctor doctor = securityService.getCurrentDoctor();
        return ResponseEntity.ok(doctorLocationRequestService.listPendingForCurrentDoctor(doctor));
    }

    @PostMapping("/location-requests/{id}/accept")
    @Operation(summary = "Accept a facility assignment request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request accepted."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Not found.")
    })
    public ResponseEntity<Void> acceptLocationRequest(@PathVariable Long id) {
        Doctor doctor = securityService.getCurrentDoctor();
        doctorLocationRequestService.accept(id, doctor);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/location-requests/{id}/reject")
    @Operation(summary = "Reject a facility assignment request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request rejected."),
            @ApiResponse(responseCode = "403", description = "Forbidden."),
            @ApiResponse(responseCode = "404", description = "Not found.")
    })
    public ResponseEntity<Void> rejectLocationRequest(@PathVariable Long id) {
        Doctor doctor = securityService.getCurrentDoctor();
        doctorLocationRequestService.reject(id, doctor);
        return ResponseEntity.ok().build();
    }
}
