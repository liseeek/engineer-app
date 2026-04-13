package com.example.medhub.controller;

import com.example.medhub.dto.DoctorDto;
import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.dto.request.DoctorSignupRequestDto;
import com.example.medhub.dto.request.OperationType;
import com.example.medhub.dto.request.UpdateDoctorLocationRequestDto;
import com.example.medhub.entity.Admin;
import com.example.medhub.entity.User;
import com.example.medhub.enums.DoctorVerificationStatus;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.service.DoctorCrudService;
import com.example.medhub.service.DoctorSignupService;
import com.example.medhub.service.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;

@RestController
@RequestMapping("/v1/doctors")
@RequiredArgsConstructor
public class DoctorsController {
    private final DoctorCrudService doctorCrudService;
    private final DoctorSignupService doctorSignupService;
    private final SecurityService securityService;

    @PostMapping("/signup")
    @Operation(summary = "Doctor self-registration (VERIFIED after PWZ format + uniqueness checks)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doctor registered."),
            @ApiResponse(responseCode = "400", description = "Bad request.")
    })
    public ResponseEntity<Void> signupDoctor(@Valid @RequestBody DoctorSignupRequestDto request) {
        doctorSignupService.signupDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Deprecated
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add new doctor (DEPRECATED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New doctor created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request.")
    })
    public ResponseEntity<DoctorDto> addDoctor(@Valid @RequestBody DoctorCreateRequestDto newDoctor) {
        DoctorDto created = doctorCrudService.saveDoctor(newDoctor);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Return doctors (optional filter by verification status)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<Page<DoctorDto>> getDoctors(
            @RequestParam(required = false) DoctorVerificationStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(doctorCrudService.getAllDoctors(pageable, status));
    }

    @GetMapping("{id}/locations")
    @Operation(summary = "Return locations where doctor works")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Requested location found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<List<LocationDto>> getLocationsByDoctorId(@PathVariable Long id) {
        return ResponseEntity.ok(doctorCrudService.getLocationsByDoctorId(id));
    }

    @GetMapping("/by-specialization")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Doctors by specialization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors by spec get successfully"),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public Page<DoctorDto> getDoctorsBySpecialization(
            @RequestParam Long specializationId,
            @PageableDefault(size = 20) Pageable pageable) {
        return doctorCrudService.getDoctorsBySpecialization(specializationId, pageable);
    }

    @GetMapping("/by-city-and-specialization")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get doctors by city and specialization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctors retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public Page<DoctorDto> getDoctorsByCityAndSpecialization(@RequestParam String city,
            @RequestParam Long specializationId,
            @PageableDefault(size = 20) Pageable pageable) {
        return doctorCrudService.getDoctorsByCityAndSpecialization(city, specializationId, pageable);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update doctor location (ADD: admin only; REMOVE: worker at same location or admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor location updated successfully."),
            @ApiResponse(responseCode = "404", description = "Doctor not found.")
    })
    public ResponseEntity<?> updateDoctorLocation(@PathVariable Long id,
            @RequestBody UpdateDoctorLocationRequestDto updateDoctorLocationRequestDto) {
        User current = securityService.getCurrentUser();
        if (updateDoctorLocationRequestDto.getOperationType() == OperationType.ADD) {
            if (!(current instanceof Admin)) {
                throw new MedHubServiceException("Only administrators can add a location directly; workers must send a facility request.");
            }
            doctorCrudService.addLocation(id, updateDoctorLocationRequestDto);
        } else if (updateDoctorLocationRequestDto.getOperationType() == OperationType.REMOVE) {
            doctorCrudService.removeLocation(id, updateDoctorLocationRequestDto);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete doctor by id (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted existing doctor."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        doctorCrudService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
