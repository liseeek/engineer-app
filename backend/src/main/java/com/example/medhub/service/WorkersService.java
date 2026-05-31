package com.example.medhub.service;

import com.example.medhub.dto.response.AppointmentsDto;
import com.example.medhub.dto.response.DoctorDto;
import com.example.medhub.dto.response.LocationDto;
import com.example.medhub.mapper.AppointmentsMapper;
import com.example.medhub.mapper.DoctorMapper;
import com.example.medhub.mapper.LocationMapper;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.WorkerRepository;
import com.example.medhub.entity.Worker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkersService {
    private final WorkerRepository workerRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentsRepository appointmentsRepository;
    private final DoctorMapper doctorMapper;
    private final AppointmentsMapper appointmentsMapper;
    private final LocationMapper locationMapper;
    private final SecurityService securityService;
    private final AppointmentMaintenanceService appointmentMaintenanceService;

    public LocationDto getWorkerLocation() {
        Worker worker = securityService.getCurrentWorker();
        return locationMapper.toLocationDto(worker.getLocation());
    }

    public Page<DoctorDto> getDoctorsFromWorkerLocation(Pageable pageable) {
        Worker worker = securityService.getCurrentWorker();
        return doctorRepository.findByLocationsLocationId(worker.getLocation().getLocationId(), pageable)
                .map(doctorMapper::toDoctorDto);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentsDto> getAppointmentsForCurrentWorker(Pageable pageable) {
        appointmentMaintenanceService.markPastAppointmentsCompleted();
        Worker worker = securityService.getCurrentWorker();
        return appointmentsRepository
                .findAllByLocationAndPatientIsNotNullOrderByDateAscTimeAsc(worker.getLocation(), pageable)
                .map(appointmentsMapper::toAppointmentDto);
    }
}