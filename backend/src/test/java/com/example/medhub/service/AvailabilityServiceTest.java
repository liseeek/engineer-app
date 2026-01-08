package com.example.medhub.service;

import com.example.medhub.dto.request.AvailabilityCreateRequestDto;
import com.example.medhub.entity.AppointmentType;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.DoctorEntity;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.WorkerEntity;
import com.example.medhub.mapper.AppointmentsMapper;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.DoctorRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private AppointmentsRepository appointmentsRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private AppointmentsMapper appointmentsMapper;

    @InjectMocks
    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createAvailability_ShouldGenerateCorrectSlots_WhenWorkerIsAuthenticated() {
        WorkerEntity worker = new WorkerEntity();
        LocationEntity location = new LocationEntity();
        location.setLocationId(1L);
        location.setCity("Gdańsk");
        worker.setLocation(location);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(worker);

        Long doctorId = 10L;
        DoctorEntity doctor = new DoctorEntity();
        doctor.setDoctorId(doctorId);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        AvailabilityCreateRequestDto request = new AvailabilityCreateRequestDto();
        request.setDoctorId(doctorId);
        request.setDate(LocalDate.now().plusDays(1));
        request.setFromTime(LocalTime.of(8, 0));
        request.setToTime(LocalTime.of(9, 0));
        request.setVisitTime(30L);
        request.setAppointmentType(AppointmentType.NFZ);

        availabilityService.createAvailability(request);

        ArgumentCaptor<List<AppointmentsEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(appointmentsRepository).saveAll(captor.capture());

        List<AppointmentsEntity> savedAppointments = captor.getValue();

        assertThat(savedAppointments).hasSize(2);
        
        assertThat(savedAppointments.get(0).getTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(savedAppointments.get(1).getTime()).isEqualTo(LocalTime.of(8, 30));

        assertThat(savedAppointments.get(0).getLocation().getCity()).isEqualTo("Gdańsk");
    }
}
