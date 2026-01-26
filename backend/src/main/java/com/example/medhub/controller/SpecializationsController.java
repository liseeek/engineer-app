package com.example.medhub.controller;

import com.example.medhub.dto.SpecializationDto;
import com.example.medhub.dto.request.SpecializationCreateRequestDto;
import com.example.medhub.service.SpecializationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/specializations")
public class SpecializationsController {
    private final SpecializationsService specializationsService;

    @PostMapping
    @Operation(summary = "Add new specialization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New specialization created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request.")
    })
    public ResponseEntity<?> addSpecialization(
            @RequestBody SpecializationCreateRequestDto specializationCreateRequestDto) {
        specializationsService.saveSpecialization(specializationCreateRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Return all specializations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Locations found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public List<SpecializationDto> getSpecializations() {
        return specializationsService.getSpecializations();
    }

    @GetMapping("/by-city")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get specializations available in a city")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specializations retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public List<SpecializationDto> getSpecializationsByCity(@RequestParam String city) {
        return specializationsService.getSpecializationsByCity(city);
    }
}
