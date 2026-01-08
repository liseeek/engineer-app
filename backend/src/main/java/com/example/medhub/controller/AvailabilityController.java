package com.example.medhub.controller;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.dto.request.AvailabilityCreateRequestDto;
import com.example.medhub.enums.AppointmentType;
import com.example.medhub.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/availability")
@RequiredArgsConstructor
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    @PostMapping
    @Operation(summary = "Add new availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New availability created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request.")
    })
    public ResponseEntity<?> addAvailability(@RequestBody AvailabilityCreateRequestDto availabilityCreateRequestDto) {
        availabilityService.createAvailability(availabilityCreateRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get availability by doctorId, locationId, appointmentType")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Requested availability found."),
            @ApiResponse(responseCode = "404", description = "Not found.")
    })
    public ResponseEntity<List<AppointmentsDto>> getAvailability(@RequestParam String doctorId,
                                                                 @RequestParam String locationId,
                                                                 @RequestParam AppointmentType appointmentType) {
        return ResponseEntity.ok(availabilityService.getAvailability(locationId, doctorId, appointmentType));
    }

}
