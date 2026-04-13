package com.example.medhub.service;

import com.example.medhub.dto.DoctorDto;
import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.dto.request.UpdateDoctorLocationRequestDto;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.enums.DoctorVerificationStatus;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.DoctorMapper;
import com.example.medhub.mapper.LocationMapper;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorCrudService {

    private final DoctorRepository doctorRepository;
    private final LocationRepository locationRepository;
    private final SpecializationRepository specializationRepository;
    private final DoctorMapper doctorMapper;
    private final LocationMapper locationMapper;

    @Transactional
    public DoctorDto saveDoctor(DoctorCreateRequestDto newDoctorDto) {
        Optional<LocationEntity> location = locationRepository
                .findLocationByLocationName(newDoctorDto.getLocationName());
        if (location.isEmpty()) {
            throw new MedHubServiceException("Location not found");
        }
        List<SpecializationEntity> specializations = new ArrayList<>();
        for (Long specId : newDoctorDto.getSpecializationIds()) {
            SpecializationEntity spec = specializationRepository.findById(specId)
                    .orElseThrow(() -> new MedHubServiceException("Specialization not found"));
            specializations.add(spec);
        }

        Doctor doctor = new Doctor();
        doctor.setName(newDoctorDto.getName());
        doctor.setSurname(newDoctorDto.getSurname());
        doctor.setLocations(List.of(location.get()));
        doctor.setSpecializations(specializations);

        Doctor savedDoctor = doctorRepository.save(doctor);
        return doctorMapper.toDoctorDto(savedDoctor);
    }

    public void deleteById(Long id) {
        if (doctorRepository.existsById(id)) {
            doctorRepository.deleteById(id);
        } else {
            throw new MedHubServiceException("Not found");
        }
    }

    public Page<DoctorDto> getAllDoctors(Pageable pageable, DoctorVerificationStatus verificationStatus) {
        if (verificationStatus == null) {
            return doctorRepository.findAll(pageable).map(doctorMapper::toDoctorDto);
        }
        return doctorRepository.findByVerificationStatus(verificationStatus, pageable).map(doctorMapper::toDoctorDto);
    }

    public Page<DoctorDto> getDoctorsBySpecialization(Long specializationId, Pageable pageable) {
        return doctorRepository.findDistinctBySpecializations_SpecializationId(specializationId, pageable)
                .map(doctorMapper::toDoctorDto);
    }

    public List<LocationDto> getLocationsByDoctorId(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isPresent()) {
            return doctor.get().getLocations().stream().map(locationMapper::toLocationDto).toList();
        } else {
            throw new MedHubServiceException("Not found");
        }
    }

    public Page<DoctorDto> getDoctorsByCityAndSpecialization(String city, Long specializationId, Pageable pageable) {
        return doctorRepository.findByCityAndSpecialization(city, specializationId, pageable)
                .map(doctorMapper::toDoctorDto);
    }

    public void addLocation(Long id, UpdateDoctorLocationRequestDto updateDoctorLocationRequestDto) {
        Optional<Doctor> optionalDoctor = doctorRepository.findById(id);
        Optional<LocationEntity> optionalLocation = locationRepository
                .findById(updateDoctorLocationRequestDto.getLocationId());
        if (optionalDoctor.isPresent() && optionalLocation.isPresent()) {
            Doctor doctor = optionalDoctor.get();
            LocationEntity location = optionalLocation.get();
            doctor.getLocations().add(location);
            doctorRepository.save(doctor);
        } else {
            throw new MedHubServiceException("Doctor or Location not found");
        }
    }

    public void removeLocation(Long id, UpdateDoctorLocationRequestDto updateDoctorLocationRequestDto) {
        Optional<Doctor> optionalDoctor = doctorRepository.findById(id);
        Optional<LocationEntity> optionalLocation = locationRepository
                .findById(updateDoctorLocationRequestDto.getLocationId());
        if (optionalDoctor.isPresent() && optionalLocation.isPresent()) {
            Doctor doctor = optionalDoctor.get();
            LocationEntity location = optionalLocation.get();
            doctor.getLocations().removeIf(location::equals);
            doctorRepository.save(doctor);
        } else {
            throw new MedHubServiceException("Doctor or Location not found");
        }
    }
}
