package com.example.medhub.controller;

import com.example.medhub.dto.request.RescheduleRequestDto;
import com.example.medhub.service.AppointmentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/appointments")
public class AppointmentsController {
    private final AppointmentsService appointmentsService;

    @PatchMapping("{id}")
    @Operation(summary = "Make an appointment from availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New appointment created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request.")
    })
    public ResponseEntity<?> addAppointment(@PathVariable Long id) {
        appointmentsService.addAppointmentToUser(id);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel an appointment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Appointment canceled successfully."),
            @ApiResponse(responseCode = "404", description = "Appointment not found.")
    })
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        appointmentsService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark an appointment as completed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Appointment completed successfully."),
            @ApiResponse(responseCode = "404", description = "Appointment not found.")
    })
    public ResponseEntity<Void> completeAppointment(@PathVariable Long id) {
        appointmentsService.completeAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reschedule")
    @Operation(summary = "Reschedule an appointment to a different time slot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Appointment rescheduled successfully."),
            @ApiResponse(responseCode = "404", description = "Appointment not found.")
    })
    public ResponseEntity<Void> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequestDto dto) {
        appointmentsService.rescheduleAppointment(id, dto.newSlotId());
        return ResponseEntity.noContent().build();
    }
}
