package com.example.medhub.controller;

import com.example.medhub.dto.response.InvitationDetailsDto;
import com.example.medhub.entity.Invitation;
import com.example.medhub.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.medhub.dto.request.InvitationRegistrationRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/v1/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    @GetMapping("/{token}")
    public ResponseEntity<InvitationDetailsDto> validateInvitation(@PathVariable String token) {
        Invitation invitation = invitationService.validateToken(token);

        return ResponseEntity.ok(InvitationDetailsDto.builder()
                .email(invitation.getEmail())
                .role(invitation.getRole())
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(
            @RequestBody @Valid InvitationRegistrationRequestDto request) {
        invitationService.registerUserWithInvitation(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
