package com.example.medhub.controller;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.dto.DoctorDto;
import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.request.WorkerCreateRequestDTO;
import com.example.medhub.service.WorkersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/workers")
@RequiredArgsConstructor
public class WorkersController {
    private final WorkersService workersService;

    @PostMapping("/signup")
    @Operation(summary = "Create new medical worker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New worker created successfully."),
            @ApiResponse(responseCode = "400", description = "Worker already exists.")
    })
    public ResponseEntity<?> addWorker(@RequestBody WorkerCreateRequestDTO workerCreateRequestDTO) {
        HttpStatus httpStatus = HttpStatus.CREATED;
        try {
            workersService.saveWorker(workerCreateRequestDTO);
        } catch (Exception exception) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(httpStatus);
    }

    @GetMapping("/currentWorker/appointments")
    @Operation(summary = "Return all appointments for current worker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointments found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<List<AppointmentsDto>> getAppointmentsForCurrentWorker() {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            return new ResponseEntity<>(workersService.getAppointmentsForCurrentWorker(), httpStatus);
        } catch (Exception exception) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(httpStatus);
    }

    @GetMapping("/currentWorker/doctors")
    @Operation(summary = "Return all doctors who work at the worker's facility")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<List<DoctorDto>> getDoctorsFromWorkerLocation() {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            return new ResponseEntity<>(workersService.getDoctorsFromWorkerLocation(), httpStatus);
        } catch (Exception exception) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(httpStatus);
    }

    @GetMapping("/currentWorker/location")
    @Operation(summary = "Return location where worker works")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<LocationDto> getWorkerLocation() {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            return new ResponseEntity<>(workersService.getWorkerLocation(), httpStatus);
        } catch (Exception exception) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(httpStatus);
    }
}
