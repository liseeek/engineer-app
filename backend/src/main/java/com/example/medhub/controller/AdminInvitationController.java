package com.example.medhub.controller;

import com.example.medhub.dto.request.InvitationRequestDto;
import com.example.medhub.entity.User;
import com.example.medhub.service.InvitationCreationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/invitations")
@RequiredArgsConstructor
public class AdminInvitationController {

    private final InvitationCreationService invitationCreationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> sendInvitation(
            @RequestBody @Valid InvitationRequestDto request,
            @AuthenticationPrincipal User currentUser) {

        invitationCreationService.createInvitation(request, currentUser);
        return ResponseEntity.accepted().build();
    }
}
