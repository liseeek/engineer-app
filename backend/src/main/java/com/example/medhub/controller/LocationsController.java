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
        HttpStatus httpStatus = HttpStatus.CREATED;
        try {
            locationsService.saveLocation(locationCreateRequestDto);
            return new ResponseEntity<>(httpStatus);
        } catch (Exception exception) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(httpStatus);
    }

    @GetMapping
    @Operation(summary = "Return all locations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Locations found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<List<LocationDto>> getLocations() {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            return new ResponseEntity<>(locationsService.getLocations(), httpStatus);
        } catch (Exception exception) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(httpStatus);
    }

    @GetMapping("/cities/distinct")
    @Operation(summary = "Return distinct locations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Locations found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<List<String>> getDistinctLocations() {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            return new ResponseEntity<>(locationsService.getDistinctLocations(), httpStatus);
        } catch (Exception exception) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(httpStatus);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete location by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted existing location."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<?> deleteLocation(@PathVariable Long id) {
        HttpStatus httpStatus = HttpStatus.NO_CONTENT;
        try {
            locationsService.deleteById(id);
            return new ResponseEntity<>(httpStatus);
        } catch (Exception exception) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(httpStatus);
    }
}
