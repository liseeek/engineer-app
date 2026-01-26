package com.example.medhub.service;

import com.example.medhub.dto.request.InvitationRegistrationRequestDto;
import com.example.medhub.dto.request.InvitationRequestDto;
import com.example.medhub.entity.Invitation;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.entity.User;
import com.example.medhub.enums.InvitationStatus;
import com.example.medhub.repository.InvitationRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.SpecializationRepository;
import com.example.medhub.repository.UserRepository;
import com.example.medhub.service.strategy.UserRegistrationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final SpecializationRepository specializationRepository;
    private final EmailService emailService;
    private final List<UserRegistrationStrategy> registrationStrategies;

    @Transactional
    public void registerUserWithInvitation(InvitationRegistrationRequestDto request) {
        Invitation invitation = validateToken(request.getToken());

        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        UserRegistrationStrategy strategy = registrationStrategies.stream()
                .filter(s -> s.supports(invitation.getRole()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No strategy found for role: " + invitation.getRole()));

        strategy.register(request, invitation);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        log.info("User registered via invitation: {}", invitation.getEmail());
    }

    @Transactional
    public void createInvitation(InvitationRequestDto request, User createdBy) {
        log.info("Creating invitation for email: {} with role: {}", request.getEmail(), request.getRole());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        LocationEntity location = null;
        SpecializationEntity specialization = null;

        if ("WORKER".equalsIgnoreCase(request.getRole())) {
            if (request.getLocationId() == null) {
                throw new IllegalArgumentException("Location ID is required for WORKER invitation");
            }
            location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("Location not found"));
        } else if ("DOCTOR".equalsIgnoreCase(request.getRole())) {
            if (request.getSpecializationId() == null) {
                throw new IllegalArgumentException("Specialization ID is required for DOCTOR invitation");
            }
            if (request.getPwz() == null || request.getPwz().isBlank()) {
                throw new IllegalArgumentException("PWZ is required for DOCTOR invitation");
            }
            specialization = specializationRepository.findById(request.getSpecializationId())
                    .orElseThrow(() -> new IllegalArgumentException("Specialization not found"));
        } else {
            throw new IllegalArgumentException("Invalid role. Must be WORKER or DOCTOR");
        }

        Invitation invitation = Invitation.builder()
                .email(request.getEmail())
                .role(request.getRole().toUpperCase())
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .status(InvitationStatus.PENDING)
                .createdBy(createdBy)
                .location(location)
                .specialization(specialization)
                .pwz(request.getPwz())
                .build();

        invitationRepository.save(invitation);

        emailService.sendInvitationEmail(invitation.getEmail(), invitation.getRole(), invitation.getToken());
        log.info("Invitation created and email sent to {}", invitation.getEmail());
    }

    @Transactional(readOnly = true)
    public Invitation validateToken(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Invitation is no longer pending (Status: " + invitation.getStatus() + ")");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new IllegalArgumentException("Invitation has expired");
        }

        return invitation;
    }
}
