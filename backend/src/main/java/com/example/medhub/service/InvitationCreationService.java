package com.example.medhub.service;

import com.example.medhub.dto.request.InvitationRequestDto;
import com.example.medhub.entity.Invitation;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.User;
import com.example.medhub.enums.InvitationStatus;
import com.example.medhub.repository.InvitationRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.SpecializationRepository;
import com.example.medhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationCreationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final SpecializationRepository specializationRepository;
    private final EmailService emailService;

    @Transactional
    public void createInvitation(InvitationRequestDto request, User createdBy) {
        log.info("Creating invitation for email: {} with role: {}", request.getEmail(), request.getRole());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        if (!"WORKER".equalsIgnoreCase(request.getRole()) && !"DOCTOR".equalsIgnoreCase(request.getRole())) {
            throw new IllegalArgumentException("Only WORKER and DOCTOR invitations are supported");
        }

        if (request.getLocationId() == null) {
            throw new IllegalArgumentException("Location ID is required for WORKER invitation");
        }
        LocationEntity location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        Invitation.InvitationBuilder invitationBuilder = Invitation.builder()
                .email(request.getEmail())
                .role(request.getRole().toUpperCase())
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .status(InvitationStatus.PENDING)
                .createdBy(createdBy)
                .location(location);

        if ("DOCTOR".equalsIgnoreCase(request.getRole())) {
            if (request.getSpecializationId() != null) {
                invitationBuilder.specialization(specializationRepository.findById(request.getSpecializationId())
                        .orElseThrow(() -> new IllegalArgumentException("Specialization not found")));
            }
            invitationBuilder.pwz(request.getPwz());
        }

        Invitation invitation = invitationBuilder.build();

        invitationRepository.save(invitation);

        emailService.sendInvitationEmail(invitation.getEmail(), invitation.getRole(), invitation.getToken());
        log.info("Invitation created and email sent to {}", invitation.getEmail());
    }
}
