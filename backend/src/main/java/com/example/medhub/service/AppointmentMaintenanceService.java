package com.example.medhub.service;

import com.example.medhub.repository.AppointmentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Keeps assigned appointments in sync with real time: transitions {@code ACTIVE}/{@code RESCHEDULED}
 * visits whose {@code date+time <= now} into {@code COMPLETED}. Runs on schedule and on-demand
 * (lazy trigger) from services that render appointment lists.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentMaintenanceService {

    private final AppointmentsRepository appointmentsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int markPastAppointmentsCompleted() {
        int updated = appointmentsRepository.markPastAppointmentsCompleted(LocalDate.now(), LocalTime.now());
        if (updated > 0) {
            log.info("Marked {} appointments as COMPLETED", updated);
        }
        return updated;
    }

    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT30S")
    public void scheduledMarkPastAppointmentsCompleted() {
        markPastAppointmentsCompleted();
    }
}
