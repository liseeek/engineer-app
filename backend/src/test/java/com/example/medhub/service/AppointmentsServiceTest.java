package com.example.medhub.service;

import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Patient;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.repository.AppointmentsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentsServiceTest {

    @Mock
    private AppointmentsRepository appointmentsRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private AppointmentsService appointmentsService;

    private Patient testUser;
    private AppointmentsEntity testAppointment;

    @BeforeEach
    void setUp() {
        testUser = new Patient();
        testUser.setUserId(1L);
        testUser.setEmail("test@user.com");

        testAppointment = new AppointmentsEntity();
        testAppointment.setAppointmentId(1L);
        testAppointment.setAppointmentStatus(AppointmentStatus.ACTIVE);
        testAppointment.setPatient(null);
    }

    @Test
    void addAppointmentToUser_success() {
        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentsRepository.save(any(AppointmentsEntity.class))).thenReturn(testAppointment);

        appointmentsService.addAppointmentToUser(1L);

        assertNotNull(testAppointment.getPatient());
        assertEquals(testUser.getUserId(), testAppointment.getPatient().getUserId());
        verify(appointmentsRepository, times(1)).save(testAppointment);
    }

    @Test
    void addAppointmentToUser_appointmentNotFound_throwsMedHubServiceException() {
        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MedHubServiceException.class, () -> appointmentsService.addAppointmentToUser(1L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void addAppointmentToUser_appointmentAlreadyTaken_throwsMedHubServiceException() {
        testAppointment.setPatient(new Patient());
        when(securityService.getCurrentPatient()).thenReturn(testUser);
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        assertThrows(MedHubServiceException.class, () -> appointmentsService.addAppointmentToUser(1L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void completeAppointment_success() {
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentsRepository.save(any(AppointmentsEntity.class))).thenReturn(testAppointment);

        appointmentsService.completeAppointment(1L);

        assertEquals(AppointmentStatus.COMPLETED, testAppointment.getAppointmentStatus());
        verify(appointmentsRepository, times(1)).save(testAppointment);
    }

    @Test
    void completeAppointment_appointmentNotFound_throwsEntityNotFoundException() {
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> appointmentsService.completeAppointment(1L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }

    @Test
    void cancelAppointment_success() {
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentsRepository.save(any(AppointmentsEntity.class))).thenReturn(testAppointment);

        appointmentsService.cancelAppointment(1L);

        assertEquals(AppointmentStatus.CANCELED, testAppointment.getAppointmentStatus());
        verify(appointmentsRepository, times(1)).save(testAppointment);
    }

    @Test
    void cancelAppointment_appointmentNotFound_throwsEntityNotFoundException() {
        when(appointmentsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> appointmentsService.cancelAppointment(1L));
        verify(appointmentsRepository, never()).save(any(AppointmentsEntity.class));
    }
}
