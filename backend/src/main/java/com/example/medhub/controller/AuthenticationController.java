package com.example.medhub.controller;

import com.example.medhub.dto.request.AuthenticationRequest;
import com.example.medhub.dto.request.AuthenticationResponse;
import com.example.medhub.service.SignInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class AuthenticationController {
    private final SignInService signInService;

    @PostMapping("/signin")
    @Operation(summary = "Sign in the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signed in successfully."),
            @ApiResponse(responseCode = "401", description = "Bad credentials.")
    })
    public ResponseEntity<AuthenticationResponse> authenticateUser(@RequestBody AuthenticationRequest authenticationRequest) {
        HttpStatus httpStatus = HttpStatus.OK;
        try {
            return new ResponseEntity<>(signInService.signIn(authenticationRequest), httpStatus);
        } catch (Exception exception) {
            httpStatus = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(httpStatus);
    }
}