package com.example.medhub.controller;

import com.example.medhub.dto.DoctorDto;
import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.dto.request.OperationType;
import com.example.medhub.dto.request.UpdateDoctorLocationRequestDto;
import com.example.medhub.service.DoctorsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/doctors")
@RequiredArgsConstructor
public class DoctorsController {
    private final DoctorsService doctorsService;

    @PostMapping
    @Operation(summary = "Add new doctor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New doctor created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request.")
    })
    public ResponseEntity<?> addDoctor(@RequestBody DoctorCreateRequestDto newDoctor) {
        doctorsService.saveDoctor(newDoctor);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Return all doctors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<List<DoctorDto>> getDoctors() {
        return ResponseEntity.ok(doctorsService.getAllDoctors());
    }

    @GetMapping("{id}/locations")
    @Operation(summary = "Return locations where doctor works")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Requested location found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<List<LocationDto>> getLocationsByDoctorId(@PathVariable Long id) {
        return ResponseEntity.ok(doctorsService.getLocationsByDoctorId(id));
    }

    @GetMapping("/by-specialization")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Doctors by specialization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors by spec get successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public List<DoctorDto> getDoctorsBySpecialization(@RequestParam Long specializationId) {
        return doctorsService.getDoctorsBySpecialization(specializationId);
    }

    @GetMapping("/by-city-and-specialization")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get doctors by city and specialization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public List<DoctorDto> getDoctorsByCityAndSpecialization(@RequestParam String city,
                                                             @RequestParam Long specializationId) {
        return doctorsService.getDoctorsByCityAndSpecialization(city, specializationId);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update doctor location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor location updated successfully."),
            @ApiResponse(responseCode = "404", description = "Doctor not found.")
    })
    public ResponseEntity<?> updateDoctorLocation(@PathVariable Long id,
                                                  @RequestBody UpdateDoctorLocationRequestDto updateDoctorLocationRequestDto) {
        if (updateDoctorLocationRequestDto.getOperationType().equals(OperationType.ADD)) {
            doctorsService.addLocation(id, updateDoctorLocationRequestDto);
        }
        if (updateDoctorLocationRequestDto.getOperationType().equals(OperationType.REMOVE)) {
            doctorsService.removeLocation(id, updateDoctorLocationRequestDto);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete doctor by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted existing doctor."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        doctorsService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

