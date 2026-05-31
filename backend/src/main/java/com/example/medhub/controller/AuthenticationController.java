package com.example.medhub.controller;

import com.example.medhub.dto.request.AuthenticationRequest;
import com.example.medhub.dto.response.AuthenticationResponse;
import com.example.medhub.service.SignInService;
import com.example.medhub.service.LogoutService;
import org.springframework.http.HttpHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class AuthenticationController {
    private final SignInService signInService;
    private final LogoutService logoutService;

    @PostMapping("/signin")
    @Operation(summary = "Sign in the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signed in successfully."),
            @ApiResponse(responseCode = "401", description = "Bad credentials.")
    })
    public ResponseEntity<AuthenticationResponse> authenticateUser(
            @Valid @RequestBody AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(signInService.signIn(authenticationRequest));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout the user by blacklisting the token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logged out successfully.")
    })
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        logoutService.logout(token);
        return ResponseEntity.noContent().build();
    }
}