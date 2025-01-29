package com.example.medhub.controller;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.dto.request.UserCreateRequestDto;
import com.example.medhub.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UsersController {
    private final UsersService usersService;

    @PostMapping(path = "/signup")
    @Operation(summary = "Create new service user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New user created successfully."),
            @ApiResponse(responseCode = "400", description = "User already exists.")
    })
    public ResponseEntity<?> addUser(@RequestBody UserCreateRequestDto newUser) {
        HttpStatus httpStatus = HttpStatus.CREATED;
        try {
            usersService.saveUser(newUser);
        } catch (Exception exception) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(httpStatus);
    }

    @GetMapping("/currentUser/appointments")
    @Operation(summary = "Return all appointments for current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Appointments found."),
            @ApiResponse(responseCode = "404", description = "Not Found.")
    })
    public ResponseEntity<List<AppointmentsDto>> getAppointmentsForCurrentUser() {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            return new ResponseEntity<>(usersService.getAppointmentsForCurrentUser(), httpStatus);
        } catch (Exception exception) {
            httpStatus = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(httpStatus);
    }
}