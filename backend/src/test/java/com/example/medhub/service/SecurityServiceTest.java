package com.example.medhub.service;

import com.example.medhub.entity.Patient;
import com.example.medhub.entity.User;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityService securityService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserEmail_returnsAnonymous_whenAuthenticationIsMissing() {
        assertEquals("anonymousUser", securityService.getCurrentUserEmail());
    }

    @Test
    void getCurrentUser_returnsUserFromRepository() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("patient@medhub.com", "secret", List.of()));
        User patient = new Patient();
        patient.setEmail("patient@medhub.com");
        when(userRepository.findByEmail("patient@medhub.com")).thenReturn(Optional.of(patient));

        User currentUser = securityService.getCurrentUser();

        assertEquals("patient@medhub.com", currentUser.getEmail());
    }

    @Test
    void getCurrentUser_throwsWhenUserNotFoundInRepository() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("missing@medhub.com", "secret", List.of()));
        when(userRepository.findByEmail("missing@medhub.com")).thenReturn(Optional.empty());

        MedHubServiceException exception = assertThrows(MedHubServiceException.class, securityService::getCurrentUser);

        assertEquals("User not found: missing@medhub.com", exception.getMessage());
    }

    @Test
    void getCurrentUser_throwsWhenAuthenticationIsAnonymous() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("anonymousUser", "secret", List.of()));

        MedHubServiceException exception = assertThrows(MedHubServiceException.class, securityService::getCurrentUser);

        assertEquals("User not authenticated", exception.getMessage());
    }

    @Test
    void getCurrentPatient_throwsWhenLoggedUserIsDifferentRole() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("doctor@medhub.com", "secret", List.of()));
        User notPatient = new com.example.medhub.entity.Doctor();
        notPatient.setEmail("doctor@medhub.com");
        when(userRepository.findByEmail("doctor@medhub.com")).thenReturn(Optional.of(notPatient));

        MedHubServiceException exception = assertThrows(MedHubServiceException.class, securityService::getCurrentPatient);

        assertEquals("Current user is not a Patient", exception.getMessage());
    }
}
