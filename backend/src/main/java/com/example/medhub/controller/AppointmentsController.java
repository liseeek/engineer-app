package com.example.medhub.controller;

import com.example.medhub.service.AppointmentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel an appointment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment canceled successfully."),
            @ApiResponse(responseCode = "404", description = "Appointment not found.")
    })
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        appointmentsService.cancelAppointment(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark an appointment as completed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointment completed successfully."),
            @ApiResponse(responseCode = "404", description = "Appointment not found.")
    })
    public ResponseEntity<?> completeAppointment(@PathVariable Long id) {
        appointmentsService.completeAppointment(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
