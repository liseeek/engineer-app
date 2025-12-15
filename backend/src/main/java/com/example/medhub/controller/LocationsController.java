package com.example.medhub.controller;

import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.request.LocationCreateRequestDto;
import com.example.medhub.service.LocationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/locations")
@RequiredArgsConstructor
public class LocationsController {
    private final LocationsService locationsService;

    @PostMapping
    @Operation(summary = "Add new location")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New location created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request.")
    })
    public ResponseEntity<?> addLocation(@RequestBody LocationCreateRequestDto locationCreateRequestDto) {
        locationsService.saveLocation(locationCreateRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Return all locations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Locations found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<List<LocationDto>> getLocations() {
        return ResponseEntity.ok(locationsService.getLocations());
    }

    @GetMapping("/cities/distinct")
    @Operation(summary = "Return distinct locations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Locations found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<List<String>> getDistinctLocations() {
        return ResponseEntity.ok(locationsService.getDistinctLocations());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete location by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted existing location."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<?> deleteLocation(@PathVariable Long id) {
        locationsService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
