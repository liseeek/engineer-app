package com.example.medhub.service;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.dto.request.AvailabilityCreateRequestDto;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Worker;
import com.example.medhub.enums.AppointmentType;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.AppointmentsMapper;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
    private final AppointmentsRepository appointmentsRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentsMapper appointmentsMapper;
    private final AppointmentsSlotGenerator slotGenerator;
    private final SecurityService securityService;

    @Transactional
    public List<AppointmentsDto> getAvailability(String locationId, String doctorId, AppointmentType appointmentType) {
        Long locId = Long.parseLong(locationId);
        Long docId = Long.parseLong(doctorId);
        return appointmentsRepository.findAppointmentsByFilters(locId, docId, appointmentType).stream()
                .filter(appointment -> appointment.getPatient() == null)
                .map(appointmentsMapper::toAppointmentDto).toList();
    }

    @Transactional
    public void createAvailability(AvailabilityCreateRequestDto availabilityCreateRequestDto) {
        Worker worker = securityService.getCurrentWorker();
        LocationEntity location = worker.getLocation();
        Doctor doctor = doctorRepository.findById(availabilityCreateRequestDto.getDoctorId())
                .orElseThrow(() -> new MedHubServiceException(
                        "Doctor not found with ID: " + availabilityCreateRequestDto.getDoctorId()));

        LocalTime toTime = availabilityCreateRequestDto.getToTime();
        Long visitTime = availabilityCreateRequestDto.getVisitTime();

        List<AppointmentsEntity> slots = slotGenerator.generateSlots(
                doctor,
                location,
                availabilityCreateRequestDto.getDate(),
                availabilityCreateRequestDto.getFromTime(),
                toTime,
                visitTime,
                availabilityCreateRequestDto.getAppointmentType());
        appointmentsRepository.saveAll(slots);
    }
}