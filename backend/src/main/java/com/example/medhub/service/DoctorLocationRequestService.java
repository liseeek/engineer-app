package com.example.medhub.service;

import com.example.medhub.dto.response.DoctorLocationRequestDto;
import com.example.medhub.dto.request.CreateDoctorLocationRequestDto;
import com.example.medhub.dto.request.OperationType;
import com.example.medhub.dto.request.UpdateDoctorLocationRequestDto;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.DoctorLocationRequest;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Worker;
import com.example.medhub.enums.DoctorLocationRequestStatus;
import com.example.medhub.enums.DoctorVerificationStatus;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.repository.DoctorLocationRequestRepository;
import com.example.medhub.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorLocationRequestService {

    private final DoctorLocationRequestRepository doctorLocationRequestRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorCrudService doctorCrudService;

    @Transactional
    public void createRequestFromWorker(CreateDoctorLocationRequestDto dto, Worker worker) {
        if (worker.getLocation() == null) {
            throw new MedHubServiceException("Worker must be assigned to a location");
        }
        LocationEntity location = worker.getLocation();
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new MedHubServiceException("Doctor not found"));
        if (doctor.getVerificationStatus() != DoctorVerificationStatus.VERIFIED) {
            throw new MedHubServiceException("Doctor must be verified");
        }
        if (doctor.getLocations().stream().anyMatch(l -> l.getLocationId().equals(location.getLocationId()))) {
            throw new MedHubServiceException("Doctor already works at this location");
        }
        if (doctorLocationRequestRepository
                .findByDoctorAndLocationAndStatus(doctor, location, DoctorLocationRequestStatus.PENDING)
                .isPresent()) {
            throw new MedHubServiceException("A pending request already exists for this doctor and location");
        }

        DoctorLocationRequest request = DoctorLocationRequest.builder()
                .doctor(doctor)
                .location(location)
                .status(DoctorLocationRequestStatus.PENDING)
                .requestedBy(worker)
                .createdAt(LocalDateTime.now())
                .build();
        doctorLocationRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<DoctorLocationRequestDto> listPendingForCurrentDoctor(Doctor doctor) {
        return doctorLocationRequestRepository.findByDoctorAndStatusWithLocation(doctor, DoctorLocationRequestStatus.PENDING)
                .stream()
                .map(r -> new DoctorLocationRequestDto(
                        r.getId(),
                        r.getLocation().getLocationName(),
                        r.getLocation().getCity(),
                        r.getLocation().getAddress()))
                .toList();
    }

    @Transactional
    public void accept(Long requestId, Doctor doctor) {
        DoctorLocationRequest request = doctorLocationRequestRepository.findById(requestId)
                .orElseThrow(() -> new MedHubServiceException("Request not found"));
        if (!request.getDoctor().getUserId().equals(doctor.getUserId())) {
            throw new MedHubServiceException("Not allowed");
        }
        if (request.getStatus() != DoctorLocationRequestStatus.PENDING) {
            throw new MedHubServiceException("Request is no longer pending");
        }
        UpdateDoctorLocationRequestDto update = new UpdateDoctorLocationRequestDto();
        update.setLocationId(request.getLocation().getLocationId());
        update.setOperationType(OperationType.ADD);
        doctorCrudService.addLocation(doctor.getUserId(), update);

        request.setStatus(DoctorLocationRequestStatus.ACCEPTED);
        doctorLocationRequestRepository.save(request);
    }

    @Transactional
    public void reject(Long requestId, Doctor doctor) {
        DoctorLocationRequest request = doctorLocationRequestRepository.findById(requestId)
                .orElseThrow(() -> new MedHubServiceException("Request not found"));
        if (!request.getDoctor().getUserId().equals(doctor.getUserId())) {
            throw new MedHubServiceException("Not allowed");
        }
        if (request.getStatus() != DoctorLocationRequestStatus.PENDING) {
            throw new MedHubServiceException("Request is no longer pending");
        }
        request.setStatus(DoctorLocationRequestStatus.REJECTED);
        doctorLocationRequestRepository.save(request);
    }
}
