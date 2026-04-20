package com.example.medhub.service;

import com.example.medhub.repository.AppointmentsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentMaintenanceServiceTest {

    @Mock
    private AppointmentsRepository appointmentsRepository;

    @InjectMocks
    private AppointmentMaintenanceService appointmentMaintenanceService;

    @Test
    void markPastAppointmentsCompleted_forwardsCurrentDateTimeAndReturnsCount() {
        when(appointmentsRepository.markPastAppointmentsCompleted(any(LocalDate.class), any(LocalTime.class)))
                .thenReturn(3);

        int updated = appointmentMaintenanceService.markPastAppointmentsCompleted();

        assertThat(updated).isEqualTo(3);
        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalTime> timeCaptor = ArgumentCaptor.forClass(LocalTime.class);
        verify(appointmentsRepository).markPastAppointmentsCompleted(dateCaptor.capture(), timeCaptor.capture());
        assertThat(dateCaptor.getValue()).isEqualTo(LocalDate.now());
        assertThat(timeCaptor.getValue()).isNotNull();
    }

    @Test
    void markPastAppointmentsCompleted_returnsZeroWhenRepositoryReportsNoRows() {
        when(appointmentsRepository.markPastAppointmentsCompleted(any(LocalDate.class), any(LocalTime.class)))
                .thenReturn(0);

        int updated = appointmentMaintenanceService.markPastAppointmentsCompleted();

        assertThat(updated).isZero();
    }
}
