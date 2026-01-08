package com.example.medhub.service;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.dto.request.AvailabilityCreateRequestDto;
import com.example.medhub.entity.AppointmentStatus;
import com.example.medhub.entity.AppointmentType;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.DoctorEntity;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.WorkerEntity;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.AppointmentsMapper;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
    private final AppointmentsRepository appointmentsRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentsMapper appointmentsMapper;

    @Transactional
    public List<AppointmentsDto> getAvailability(String locationId, String doctorId, AppointmentType appointmentType) {
        Long locId = Long.parseLong(locationId);
        Long docId = Long.parseLong(doctorId);
        return appointmentsRepository.findAppointmentsByFilters(locId, docId, appointmentType).stream()
                .filter(appointment -> appointment.getUser() == null)
                .map(appointmentsMapper::toAppointmentDto).toList();
    }

    @Transactional
    public void createAvailability(AvailabilityCreateRequestDto availabilityCreateRequestDto) {
        Object authenticatedUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof WorkerEntity worker) {
            LocationEntity location = worker.getLocation();
            DoctorEntity doctor = doctorRepository.findById(availabilityCreateRequestDto.getDoctorId())
                    .orElseThrow(() -> new MedHubServiceException(
                            "Doctor not found with ID: " + availabilityCreateRequestDto.getDoctorId()));

            LocalTime toTime = availabilityCreateRequestDto.getToTime();
            Long visitTime = availabilityCreateRequestDto.getVisitTime();

            List<AppointmentsEntity> appointmentsEntities = new ArrayList<>();
            for (LocalTime fromTime = availabilityCreateRequestDto.getFromTime(); fromTime
                    .isBefore(toTime); fromTime = fromTime.plusMinutes(visitTime)) {
                AppointmentsEntity appointment = new AppointmentsEntity();
                appointment.setDoctor(doctor);
                appointment.setDate(availabilityCreateRequestDto.getDate());
                appointment.setTime(fromTime);
                appointment.setLocation(location);
                appointment.setAppointmentStatus(AppointmentStatus.ACTIVE);
                appointment.setAppointmentType(availabilityCreateRequestDto.getAppointmentType());
                appointmentsEntities.add(appointment);
            }
            appointmentsRepository.saveAll(appointmentsEntities);
        } else {
            throw new MedHubServiceException("Not found");
        }
    }
}
