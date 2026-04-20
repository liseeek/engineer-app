package com.example.medhub.service;

import com.example.medhub.dto.request.UserCreateRequestDto;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Patient;
import com.example.medhub.entity.User;
import com.example.medhub.enums.Authority;
import com.example.medhub.mapper.AppointmentsMapper;
import com.example.medhub.mapper.UserMapper;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AppointmentsRepository appointmentsRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AppointmentsMapper appointmentsMapper;
    @Mock
    private SecurityService securityService;
    @Mock
    private CryptoService cryptoService;
    @Mock
    private AppointmentMaintenanceService appointmentMaintenanceService;

    @InjectMocks
    private UsersService usersService;

    @Test
    void saveUser_setsPatientAuthorityAndPeselHash() {
        UserCreateRequestDto request = new UserCreateRequestDto();
        request.setName("Jan");
        request.setSurname("Kowalski");
        request.setEmail("jan@medhub.com");
        request.setPassword("StrongPass1!");
        request.setPasswordConfirmation("StrongPass1!");
        request.setPhoneNumber("123456789");
        request.setPesel("98010112345");

        Patient mappedPatient = new Patient();
        when(passwordEncoder.encode("StrongPass1!")).thenReturn("hashed-pass");
        when(userMapper.toUser(request, "hashed-pass")).thenReturn(mappedPatient);
        when(cryptoService.hash("98010112345")).thenReturn("hashed-pesel");

        usersService.saveUser(request);

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(userRepository).save(captor.capture());
        Patient saved = captor.getValue();
        assertEquals(Authority.ROLE_PATIENT, saved.getAuthority());
        assertEquals("hashed-pesel", saved.getPeselHash());
    }

    @Test
    void getAppointmentsForCurrentUser_loadsByCurrentUserId() {
        Patient current = new Patient();
        current.setUserId(12L);
        AppointmentsEntity appointment = new AppointmentsEntity();
        when(securityService.getCurrentUser()).thenReturn(current);
        when(appointmentsRepository.findAllByPatientUserId(12L)).thenReturn(List.of(appointment));

        usersService.getAppointmentsForCurrentUser();

        verify(appointmentsRepository).findAllByPatientUserId(12L);
        verify(appointmentsMapper).toAppointmentDto(appointment);
    }

    @Test
    void deleteById_throwsWhenUserDoesNotExist() {
        when(userRepository.existsById(44L)).thenReturn(false);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> usersService.deleteById(44L));

        assertTrue(exception.getMessage().contains("44"));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteById_deletesWhenUserExists() {
        when(userRepository.existsById(44L)).thenReturn(true);

        usersService.deleteById(44L);

        verify(userRepository).deleteById(44L);
    }

    @Test
    void getUsers_withSearch_usesSearchRepositoryMethod() {
        when(userRepository.searchUsers(eq("anna"), any())).thenReturn(org.springframework.data.domain.Page.empty());

        usersService.getUsers(" anna ", org.springframework.data.domain.PageRequest.of(0, 10));

        verify(userRepository).searchUsers(eq("anna"), any());
    }
}
