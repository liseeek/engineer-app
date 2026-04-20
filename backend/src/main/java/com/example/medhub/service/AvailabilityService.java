package com.example.medhub.service;

import com.example.medhub.dto.response.AppointmentsDto;
import com.example.medhub.dto.response.DoctorDto;
import com.example.medhub.dto.response.LocationDto;
import com.example.medhub.dto.request.AvailabilityCreateRequestDto;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Worker;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.enums.AppointmentType;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.LocationMapper;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
    private final AppointmentsRepository appointmentsRepository;
    private final DoctorRepository doctorRepository;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final AppointmentsSlotGenerator slotGenerator;
    private final SecurityService securityService;

    @Transactional
    public List<AppointmentsDto> getAvailability(String locationId, String doctorId, AppointmentType appointmentType) {
        Long locId = Long.parseLong(locationId);
        Long docId = Long.parseLong(doctorId);
        List<Object[]> rows = appointmentsRepository.findAppointmentSlotRowsByFilters(
                locId, docId, appointmentType.name(), LocalDate.now(), LocalTime.now());
        if (rows.isEmpty()) {
            return List.of();
        }

        java.util.Set<Long> doctorIds = rows.stream()
                .map(r -> ((Number) r[1]).longValue())
                .collect(Collectors.toSet());
        java.util.Set<Long> locationIds = rows.stream()
                .map(r -> ((Number) r[4]).longValue())
                .collect(Collectors.toSet());

        Map<Long, Doctor> doctorsById = doctorRepository.findAllById(doctorIds).stream()
                .collect(Collectors.toMap(Doctor::getUserId, Function.identity()));
        Map<Long, LocationEntity> locationsById = locationRepository.findAllById(locationIds).stream()
                .collect(Collectors.toMap(LocationEntity::getLocationId, Function.identity()));

        return rows.stream().map(r -> {
            Long appointmentId = ((Number) r[0]).longValue();
            Long rowDoctorId = ((Number) r[1]).longValue();
            LocalDate date = toLocalDate(r[2]);
            LocalTime time = toLocalTime(r[3]);
            Long rowLocationId = ((Number) r[4]).longValue();
            AppointmentStatus status = AppointmentStatus.valueOf(Objects.toString(r[5]));
            AppointmentType type = AppointmentType.valueOf(Objects.toString(r[6]));

            Doctor doctor = doctorsById.get(rowDoctorId);
            LocationEntity location = locationsById.get(rowLocationId);
            if (doctor == null || location == null) {
                throw new MedHubServiceException("Missing doctor or location for availability row.");
            }

            DoctorDto doctorDto = new DoctorDto(
                    doctor.getUserId(), doctor.getName(), doctor.getSurname(),
                    doctor.getBio(), doctor.getAvatarUrl(), List.of(), List.of());
            LocationDto locationDto = locationMapper.toLocationDto(location);

            return new AppointmentsDto(appointmentId, null, doctorDto, date, time, locationDto, status, type, null);
        }).toList();
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        throw new IllegalArgumentException("Unsupported date type: " + (value == null ? "null" : value.getClass()));
    }

    private static LocalTime toLocalTime(Object value) {
        if (value instanceof LocalTime localTime) {
            return localTime;
        }
        if (value instanceof Time sqlTime) {
            return sqlTime.toLocalTime();
        }
        throw new IllegalArgumentException("Unsupported time type: " + (value == null ? "null" : value.getClass()));
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