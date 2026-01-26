package com.example.medhub.controller;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.dto.VisitNoteRequestDto;
import com.example.medhub.service.DoctorWorkspaceService;
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
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorWorkspaceController {

    private final DoctorWorkspaceService doctorWorkspaceService;

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
}
