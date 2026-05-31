package com.example.medhub.controller;

import com.example.medhub.dto.response.DoctorDto;
import com.example.medhub.dto.response.LocationDto;
import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.dto.request.DoctorSignupRequestDto;
import com.example.medhub.dto.request.UpdateDoctorLocationRequestDto;
import com.example.medhub.enums.DoctorVerificationStatus;
import com.example.medhub.service.DoctorCrudService;
import com.example.medhub.service.DoctorSignupService;
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

import org.springframework.lang.Nullable;

@RestController
@RequestMapping("/v1/doctors")
@RequiredArgsConstructor
public class DoctorsController {
    private final DoctorCrudService doctorCrudService;
    private final DoctorSignupService doctorSignupService;

    @PostMapping("/signup")
    @Operation(summary = "Doctor self-registration (PENDING until admin verification)")
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

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search verified doctors by city, specialization, and/or name/surname")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results returned."),
    })
    public Page<DoctorDto> searchDoctors(
            @RequestParam(required = false) @Nullable String city,
            @RequestParam(required = false) @Nullable Long specializationId,
            @RequestParam(required = false) @Nullable String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return doctorCrudService.searchDoctors(city, specializationId, q, pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Return a single doctor by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Doctor found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public DoctorDto getDoctorById(@PathVariable Long id) {
        return doctorCrudService.getDoctorById(id);
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
        doctorCrudService.updateDoctorLocation(id, updateDoctorLocationRequestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify or reject doctor (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Doctor status updated."),
            @ApiResponse(responseCode = "404", description = "Doctor not found.")
    })
    public ResponseEntity<Void> verifyDoctor(@PathVariable Long id, @RequestParam DoctorVerificationStatus status) {
        doctorCrudService.verifyDoctor(id, status);
        return ResponseEntity.noContent().build();
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
