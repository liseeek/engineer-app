package com.example.medhub.controller;

import com.example.medhub.dto.response.AppointmentsDto;
import com.example.medhub.dto.response.DoctorDto;
import com.example.medhub.dto.response.LocationDto;
import com.example.medhub.dto.request.CreateDoctorLocationRequestDto;
import com.example.medhub.dto.request.WorkerCreateRequestDto;
import com.example.medhub.entity.Worker;
import com.example.medhub.service.DoctorLocationRequestService;
import com.example.medhub.service.SecurityService;
import com.example.medhub.service.WorkersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/v1/workers")
@RequiredArgsConstructor
public class WorkersController {
    private final WorkersService workersService;
    private final DoctorLocationRequestService doctorLocationRequestService;
    private final SecurityService securityService;

    @PostMapping("/signup")
    @Operation(summary = "Create new medical worker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New worker created successfully."),
            @ApiResponse(responseCode = "400", description = "Worker already exists.")
    })
    public ResponseEntity<?> addWorker(@Valid @RequestBody WorkerCreateRequestDto workerCreateRequestDto) {
        workersService.saveWorker(workerCreateRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/currentWorker/appointments")
    @Operation(summary = "Return all appointments for current worker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointments found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<Page<AppointmentsDto>> getAppointmentsForCurrentWorker(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(workersService.getAppointmentsForCurrentWorker(pageable));
    }

    @GetMapping("/currentWorker/doctors")
    @Operation(summary = "Return all doctors who work at the worker's facility")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<Page<DoctorDto>> getDoctorsFromWorkerLocation(
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(workersService.getDoctorsFromWorkerLocation(pageable));
    }

    @GetMapping("/currentWorker/location")
    @Operation(summary = "Return location where worker works")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<LocationDto> getWorkerLocation() {
        return ResponseEntity.ok(workersService.getWorkerLocation());
    }

    @PostMapping("/doctor-location-requests")
    @PreAuthorize("hasRole('WORKER')")
    @Operation(summary = "Request linking a doctor to the worker's facility (doctor must accept)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Request created."),
            @ApiResponse(responseCode = "400", description = "Bad request.")
    })
    public ResponseEntity<Void> createDoctorLocationRequest(@RequestBody @Valid CreateDoctorLocationRequestDto request) {
        Worker worker = securityService.getCurrentWorker();
        doctorLocationRequestService.createRequestFromWorker(request, worker);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
