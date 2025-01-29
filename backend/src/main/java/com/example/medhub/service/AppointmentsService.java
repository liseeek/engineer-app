package com.example.medhub.service;

import com.example.medhub.entity.AppointmentStatus;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.UserEntity;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.repository.AppointmentsRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentsService {
    private final AppointmentsRepository appointmentsRepository;

    @Transactional
    public void addAppointmentToUser(Long appointmentId) {
        Object authenticatedUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof UserEntity user) {
            AppointmentsEntity appointment = appointmentsRepository.findById(appointmentId)
                    .orElseThrow(() -> new MedHubServiceException("Not found"));
            if (appointment.getUser() != null) {
                throw new MedHubServiceException("Availability already assigned");
            }
            appointment.setUser(user);
            appointmentsRepository.save(appointment);
        } else {
            throw new MedHubServiceException("Not found");
        }
    }

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

