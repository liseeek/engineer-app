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
import com.example.medhub.service.strategy.UserRegistrationStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private SpecializationRepository specializationRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private List<UserRegistrationStrategy> registrationStrategies;

    @InjectMocks
    private InvitationService invitationService;

    @Test
    void shouldCreateInvitation_WhenWorkerRequestIsValid() {
        InvitationRequestDto request = InvitationRequestDto.builder()
                .email("worker@test.com")
                .role("WORKER")
                .locationId(1L)
                .build();
        User admin = mock(User.class);
        LocationEntity location = new LocationEntity();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        invitationService.createInvitation(request, admin);

        ArgumentCaptor<Invitation> captor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(captor.capture());
        Invitation saved = captor.getValue();

        assertEquals("worker@test.com", saved.getEmail());
        assertEquals("WORKER", saved.getRole());
        assertEquals(InvitationStatus.PENDING, saved.getStatus());
        assertNotNull(saved.getToken());

        verify(emailService).sendInvitationEmail(eq("worker@test.com"), eq("WORKER"), anyString());
    }

    @Test
    void shouldRejectInvitation_WhenEmailAlreadyExists() {
        InvitationRequestDto request = InvitationRequestDto.builder()
                .email("existing@test.com")
                .role("WORKER")
                .locationId(1L)
                .build();
        User admin = mock(User.class);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> invitationService.createInvitation(request, admin));

        assertEquals("User with this email already exists", exception.getMessage());
        verify(invitationRepository, never()).save(any());
    }
}
