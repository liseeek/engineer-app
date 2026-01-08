package com.example.medhub.service;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.dto.DoctorDto;
import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.request.WorkerCreateRequestDTO;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Authority;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.WorkerEntity;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.AppointmentsMapper;
import com.example.medhub.mapper.DoctorMapper;
import com.example.medhub.mapper.LocationMapper;
import com.example.medhub.mapper.WorkerMapper;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.UserRepository;
import com.example.medhub.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
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
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final LocationRepository locationRepository;
    private final AppointmentsRepository appointmentsRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorMapper doctorMapper;
    private final AppointmentsMapper appointmentsMapper;
    private final WorkerMapper workerMapper;
    private final LocationMapper locationMapper;

    public void saveWorker(WorkerCreateRequestDTO workerCreateRequestDTO) {
        if (userRepository.existsByEmail(workerCreateRequestDTO.getEmail())) {
            throw new MedHubServiceException("Email already exists");
        }

        Optional<LocationEntity> location = locationRepository
                .findLocationByLocationName(workerCreateRequestDTO.getLocationName());

        if (location.isEmpty()) {
            throw new MedHubServiceException("Location not found");
        } else {
            var encryptedPassword = passwordEncoder.encode(workerCreateRequestDTO.getPassword());
            WorkerEntity workerEntity = workerMapper.toWorker(workerCreateRequestDTO, encryptedPassword);
            workerEntity.setAuthority(Authority.ROLE_WORKER);
            workerEntity.setLocation(location.get());
            workerRepository.save(workerEntity);
        }
    }

    public LocationDto getWorkerLocation() {
        Object authenticatedUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticatedUser instanceof WorkerEntity worker) {
            return locationMapper.toLocationDto(worker.getLocation());
        } else {
            throw new MedHubServiceException("Not found");
        }
    }

    @Transactional
    public List<DoctorDto> getDoctorsFromWorkerLocation() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<WorkerEntity> worker = workerRepository.findWorkerEntitiesByEmail(email);
        if (worker.isPresent()) {
            return worker.get().getLocation().getDoctors().stream().map(doctorMapper::toDoctorDto).toList();
        } else {
            throw new MedHubServiceException("Not found");
        }
    }

    public List<AppointmentsDto> getAppointmentsForCurrentWorker() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        WorkerEntity worker = workerRepository.findWorkerEntitiesByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Worker not found with email: " + email));

        return appointmentsRepository.findAllScheduledByLocation(worker.getLocation()).stream()
                .map(appointmentsMapper::toAppointmentDto)
                .toList();
    }
}