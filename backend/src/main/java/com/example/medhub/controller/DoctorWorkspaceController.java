package com.example.medhub.controller;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.dto.DoctorLocationRequestDto;
import com.example.medhub.dto.VisitNoteRequestDto;
import com.example.medhub.entity.Doctor;
import com.example.medhub.service.DoctorLocationRequestService;
import com.example.medhub.service.DoctorWorkspaceService;
import com.example.medhub.service.SecurityService;
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
    public ResponseEntity<List<AppointmentsDto>> getMySchedule() {
        return ResponseEntity.ok(doctorWorkspaceService.getMySchedule());
    }

    @PostMapping("/appointments/{id}/note")
    public ResponseEntity<Void> concludeVisit(
            @PathVariable Long id,
            @RequestBody @Valid VisitNoteRequestDto noteDto) {
        doctorWorkspaceService.concludeVisit(id, noteDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/location-requests")
    public ResponseEntity<List<DoctorLocationRequestDto>> listPendingLocationRequests() {
        Doctor doctor = securityService.getCurrentDoctor();
        return ResponseEntity.ok(doctorLocationRequestService.listPendingForCurrentDoctor(doctor));
    }

    @PostMapping("/location-requests/{id}/accept")
    public ResponseEntity<Void> acceptLocationRequest(@PathVariable Long id) {
        Doctor doctor = securityService.getCurrentDoctor();
        doctorLocationRequestService.accept(id, doctor);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/location-requests/{id}/reject")
    public ResponseEntity<Void> rejectLocationRequest(@PathVariable Long id) {
        Doctor doctor = securityService.getCurrentDoctor();
        doctorLocationRequestService.reject(id, doctor);
        return ResponseEntity.ok().build();
    }
}
