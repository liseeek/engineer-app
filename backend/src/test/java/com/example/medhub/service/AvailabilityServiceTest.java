package com.example.medhub.service;

import com.example.medhub.dto.request.AvailabilityCreateRequestDto;
import com.example.medhub.enums.AppointmentType;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Worker;
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
    private AppointmentsSlotGenerator slotGenerator;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private AvailabilityService availabilityService;

    @Test
    void createAvailability_ShouldGenerateCorrectSlots_WhenWorkerIsAuthenticated() {
        Worker worker = new Worker();
        LocationEntity location = new LocationEntity();
        location.setLocationId(1L);
        location.setCity("Gdańsk");
        worker.setLocation(location);

        when(securityService.getCurrentWorker()).thenReturn(worker);

        Long doctorId = 10L;
        Doctor doctor = new Doctor();
        doctor.setUserId(doctorId);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        AvailabilityCreateRequestDto request = new AvailabilityCreateRequestDto();
        request.setDoctorId(doctorId);
        request.setDate(LocalDate.now().plusDays(1));
        request.setFromTime(LocalTime.of(8, 0));
        request.setToTime(LocalTime.of(9, 0));
        request.setVisitTime(30L);
        request.setAppointmentType(AppointmentType.NFZ);

        List<AppointmentsEntity> expectedSlots = List.of(
                AppointmentsEntity.builder().doctor(doctor).location(location).time(LocalTime.of(8, 0)).build(),
                AppointmentsEntity.builder().doctor(doctor).location(location).time(LocalTime.of(8, 30)).build());
        when(slotGenerator.generateSlots(any(), any(), any(), any(), any(), any(), any())).thenReturn(expectedSlots);

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
