package com.example.medhub.service;

import com.example.medhub.dto.request.InvitationRegistrationRequestDto;
import com.example.medhub.entity.Invitation;
import com.example.medhub.enums.InvitationStatus;
import com.example.medhub.repository.InvitationRepository;
import com.example.medhub.service.strategy.UserRegistrationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationRegistrationService {

    private final InvitationRepository invitationRepository;
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
