package com.example.medhub.service;

import com.example.medhub.dto.DoctorDto;
import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.SpecializationDto;
import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.dto.request.UpdateDoctorLocationRequestDto;
import com.example.medhub.entity.DoctorEntity;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorsService {
    private final DoctorRepository doctorRepository;
    private final LocationRepository locationRepository;
    private final SpecializationRepository specializationRepository;

    @Transactional
    public DoctorDto saveDoctor(DoctorCreateRequestDto newDoctorDto) {
        Optional<LocationEntity> location = locationRepository
                .findLocationByLocationName(newDoctorDto.getLocationName());
        if (location.isEmpty()) {
            throw new MedHubServiceException("Location not found");
        } else {
            SpecializationEntity specializationEntity = specializationRepository
                    .findById(newDoctorDto.getSpecializationId())
                    .orElseThrow(() -> new MedHubServiceException("Specialization not found"));

            DoctorEntity doctorEntity = new DoctorEntity();
            doctorEntity.setName(newDoctorDto.getName());
            doctorEntity.setSurname(newDoctorDto.getSurname());
            doctorEntity.setLocations(List.of(location.get()));
            doctorEntity.setSpecialization(specializationEntity);

            DoctorEntity savedDoctorEntity = doctorRepository.save(doctorEntity);

            SpecializationDto specializationDto = new SpecializationDto(
                    specializationEntity.getSpecializationId(),
                    specializationEntity.getSpecializationName());
            return DoctorDto.from(savedDoctorEntity, specializationDto);
        }
    }

    public void deleteById(Long id) {
        if (doctorRepository.existsById(id)) {
            doctorRepository.deleteById(id);
        } else {
            throw new MedHubServiceException("Not found");
        }
    }

    public List<DoctorDto> getAllDoctors() {
        List<DoctorEntity> allDoctorEntities = doctorRepository.findAll();
        return allDoctorEntities.stream()
                .map(DoctorDto::from)
                .collect(Collectors.toList());
    }

    public List<DoctorDto> getDoctorsBySpecialization(Long specializationId) {
        List<DoctorEntity> doctorEntities = doctorRepository.findBySpecialization_SpecializationId(specializationId);

        return doctorEntities.stream()
                .map(doctor -> {
                    List<LocationDto> locationDtos = doctor.getLocations().stream().map(LocationDto::from).toList();
                    return new DoctorDto(
                            doctor.getDoctorId(),
                            doctor.getName(),
                            doctor.getSurname(),
                            locationDtos,
                            SpecializationDto.from(doctor.getSpecialization()));
                })
                .collect(Collectors.toList());
    }

    public List<LocationDto> getLocationsByDoctorId(Long id) {
        Optional<DoctorEntity> doctor = doctorRepository.findById(id);
        if (doctor.isPresent()) {
            return doctor.get().getLocations().stream().map(LocationDto::from).toList();
        } else {
            throw new MedHubServiceException("Not found");
        }
    }

    public List<DoctorDto> getDoctorsByCityAndSpecialization(String city, Long specializationId) {
        List<DoctorEntity> doctors = doctorRepository.findByCityAndSpecialization(city, specializationId);

        return doctors.stream()
                .map(doctor -> {
                    List<LocationDto> locationDtos = doctor.getLocations().stream().map(LocationDto::from).toList();
                    return new DoctorDto(
                            doctor.getDoctorId(),
                            doctor.getName(),
                            doctor.getSurname(),
                            locationDtos,
                            SpecializationDto.from(doctor.getSpecialization()));
                })
                .collect(Collectors.toList());
    }

    public void addLocation(Long id, UpdateDoctorLocationRequestDto updateDoctorLocationRequestDto) {
        Optional<DoctorEntity> optionalDoctor = doctorRepository.findById(id);
        Optional<LocationEntity> optionalLocation = locationRepository
                .findById(updateDoctorLocationRequestDto.getLocationId());
        if (optionalDoctor.isPresent() && optionalLocation.isPresent()) {
            DoctorEntity doctor = optionalDoctor.get();
            LocationEntity location = optionalLocation.get();
            doctor.getLocations().add(location);
            doctorRepository.save(doctor);
        } else {
            throw new MedHubServiceException("Doctor or Location not found");
        }
    }

    public void removeLocation(Long id, UpdateDoctorLocationRequestDto updateDoctorLocationRequestDto) {
        Optional<DoctorEntity> optionalDoctor = doctorRepository.findById(id);
        Optional<LocationEntity> optionalLocation = locationRepository
                .findById(updateDoctorLocationRequestDto.getLocationId());
        if (optionalDoctor.isPresent() && optionalLocation.isPresent()) {
            DoctorEntity doctor = optionalDoctor.get();
            LocationEntity location = optionalLocation.get();
            doctor.getLocations().removeIf(location::equals);
            doctorRepository.save(doctor);
        } else {
            throw new MedHubServiceException("Doctor or Location not found");
        }
    }
}
