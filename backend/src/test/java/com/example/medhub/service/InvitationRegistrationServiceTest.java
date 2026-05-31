package com.example.medhub.service;

import com.example.medhub.dto.request.InvitationRegistrationRequestDto;
import com.example.medhub.dto.response.InvitationDetailsDto;
import com.example.medhub.entity.Invitation;
import com.example.medhub.enums.InvitationStatus;
import com.example.medhub.mapper.InvitationMapper;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.InvitationRepository;
import com.example.medhub.service.strategy.UserRegistrationStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationRegistrationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private InvitationMapper invitationMapper;
    @Mock
    private UserRegistrationStrategy workerStrategy;
    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private InvitationRegistrationService invitationRegistrationService;

    @Test
    void validateInvitation_returnsMappedDetails() {
        Invitation invitation = pendingInvitation();
        InvitationDetailsDto detailsDto = new InvitationDetailsDto();
        detailsDto.setEmail("worker@medhub.com");
        when(invitationRepository.findByToken("token-1")).thenReturn(Optional.of(invitation));
        when(invitationMapper.toInvitationDetails(invitation)).thenReturn(detailsDto);

        InvitationDetailsDto result = invitationRegistrationService.validateInvitation("token-1");

        assertEquals("worker@medhub.com", result.getEmail());
    }

    @Test
    void validateToken_marksExpiredInvitationAndThrows() {
        Invitation invitation = pendingInvitation();
        invitation.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(invitationRepository.findByToken("token-1")).thenReturn(Optional.of(invitation));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> invitationRegistrationService.validateToken("token-1"));

        assertEquals("Invitation has expired", exception.getMessage());
        assertEquals(InvitationStatus.EXPIRED, invitation.getStatus());
        verify(invitationRepository).save(invitation);
    }

    @Test
    void registerUserWithInvitation_throwsWhenPasswordsDoNotMatch() {
        Invitation invitation = pendingInvitation();
        InvitationRegistrationRequestDto request = request("Password123!", "Password2!");
        when(invitationRepository.findByToken("token-1")).thenReturn(Optional.of(invitation));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> invitationRegistrationService.registerUserWithInvitation(request));

        assertEquals("Passwords do not match", exception.getMessage());
        verify(workerStrategy, never()).register(any(), any());
    }

    @Test
    void registerUserWithInvitation_throwsWhenNoStrategySupportsRole() {
        Invitation invitation = pendingInvitation();
        InvitationRegistrationRequestDto request = request("Password123!", "Password123!");
        when(invitationRepository.findByToken("token-1")).thenReturn(Optional.of(invitation));
        when(workerStrategy.supports("WORKER")).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new InvitationRegistrationService(invitationRepository, invitationMapper, List.of(workerStrategy), doctorRepository)
                        .registerUserWithInvitation(request));

        assertEquals("No strategy found for role: WORKER", exception.getMessage());
    }

    @Test
    void registerUserWithInvitation_setsAcceptedStatusWhenStrategyHandlesRole() {
        Invitation invitation = pendingInvitation();
        InvitationRegistrationRequestDto request = request("Password123!", "Password123!");
        InvitationRegistrationService service = new InvitationRegistrationService(
                invitationRepository,
                invitationMapper,
                List.of(workerStrategy),
                doctorRepository);
        when(invitationRepository.findByToken("token-1")).thenReturn(Optional.of(invitation));
        when(workerStrategy.supports("WORKER")).thenReturn(true);

        service.registerUserWithInvitation(request);

        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        verify(workerStrategy).register(request, invitation);
        verify(invitationRepository).save(invitation);
    }

    private Invitation pendingInvitation() {
        return Invitation.builder()
                .token("token-1")
                .email("worker@medhub.com")
                .role("WORKER")
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(2))
                .build();
    }

    private InvitationRegistrationRequestDto request(String password, String confirmation) {
        InvitationRegistrationRequestDto request = new InvitationRegistrationRequestDto();
        request.setToken("token-1");
        request.setPassword(password);
        request.setPasswordConfirmation(confirmation);
        request.setName("Anna");
        request.setSurname("Nowak");
        request.setPhoneNumber("123456789");
        return request;
    }
}
