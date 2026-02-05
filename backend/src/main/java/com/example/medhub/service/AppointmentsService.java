package com.example.medhub.service;

import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Patient;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.repository.AppointmentsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentsService {
    private final AppointmentsRepository appointmentsRepository;
    private final SecurityService securityService;

    @Transactional
    public void addAppointmentToUser(Long appointmentId) {
        Patient patient = securityService.getCurrentPatient();
        AppointmentsEntity appointment = appointmentsRepository.findById(appointmentId)
                .orElseThrow(() -> new MedHubServiceException("Not found"));
        if (appointment.getPatient() != null) {
            throw new MedHubServiceException("Availability already assigned");
        }
        appointment.setPatient(patient);
        appointmentsRepository.save(appointment);
    }

    @Transactional
    public void completeAppointment(Long appointmentId) {
        Optional<AppointmentsEntity> appointment = appointmentsRepository.findById(appointmentId);
        if (appointment.isPresent()) {
            AppointmentsEntity appointmentEntity = appointment.get();
            appointmentEntity.setAppointmentStatus(AppointmentStatus.COMPLETED);
            appointmentsRepository.save(appointmentEntity);
        } else {
            throw new EntityNotFoundException("Appointment with ID: " + appointmentId + " does not exist.");
        }
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Optional<AppointmentsEntity> appointment = appointmentsRepository.findById(appointmentId);
        if (appointment.isPresent()) {
            AppointmentsEntity appointmentEntity = appointment.get();
            appointmentEntity.setAppointmentStatus(AppointmentStatus.CANCELED);
            appointmentsRepository.save(appointmentEntity);
        } else {
            throw new EntityNotFoundException("Appointment with ID: " + appointmentId + " does not exist.");
        }
    }
}
