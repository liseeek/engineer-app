package com.example.medhub.service;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.dto.DoctorDto;
import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.request.WorkerCreateRequestDTO;
import com.example.medhub.enums.Authority;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Worker;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.event.WorkerRegisteredEvent;
import com.example.medhub.mapper.AppointmentsMapper;
import com.example.medhub.mapper.DoctorMapper;
import com.example.medhub.mapper.LocationMapper;
import com.example.medhub.mapper.WorkerMapper;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkersService {
    private final WorkerRepository workerRepository;
    private final LocationRepository locationRepository;
    private final AppointmentsRepository appointmentsRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorMapper doctorMapper;
    private final AppointmentsMapper appointmentsMapper;
    private final WorkerMapper workerMapper;
    private final LocationMapper locationMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final SecurityService securityService;

    @Transactional
    public void saveWorker(WorkerCreateRequestDTO workerCreateRequestDTO) {
        Optional<LocationEntity> location = locationRepository
                .findLocationByLocationName(workerCreateRequestDTO.getLocationName());

        if (location.isEmpty()) {
            throw new MedHubServiceException("Location not found");
        } else {
            var encryptedPassword = passwordEncoder.encode(workerCreateRequestDTO.getPassword());
            Worker worker = workerMapper.toWorker(workerCreateRequestDTO, encryptedPassword);
            worker.setAuthority(Authority.ROLE_WORKER);
            worker.setLocation(location.get());
            Worker savedWorker = workerRepository.save(worker);

            String currentUserEmail = securityService.getCurrentUserEmail();

            eventPublisher.publishEvent(new WorkerRegisteredEvent(
                    this,
                    currentUserEmail,
                    savedWorker.getEmail(),
                    savedWorker.getUserId().toString()));
        }
    }

    public LocationDto getWorkerLocation() {
        Worker worker = securityService.getCurrentWorker();
        return locationMapper.toLocationDto(worker.getLocation());
    }

    @Transactional
    public List<DoctorDto> getDoctorsFromWorkerLocation() {
        Worker worker = securityService.getCurrentWorker();
        return worker.getLocation().getDoctors().stream().map(doctorMapper::toDoctorDto).toList();
    }

    public List<AppointmentsDto> getAppointmentsForCurrentWorker() {
        Worker worker = securityService.getCurrentWorker();
        return appointmentsRepository.findAllScheduledByLocation(worker.getLocation()).stream()
                .map(appointmentsMapper::toAppointmentDto)
                .toList();
    }
}