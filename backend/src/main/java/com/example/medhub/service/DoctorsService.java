package com.example.medhub.service;

import com.example.medhub.dto.DoctorDto;
import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.SpecializationDto;
import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.dto.request.UpdateDoctorLocationRequestDto;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.DoctorMapper;
import com.example.medhub.mapper.LocationMapper;
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
    private final DoctorMapper doctorMapper;
    private final LocationMapper locationMapper;

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

            Doctor doctor = new Doctor();
            doctor.setName(newDoctorDto.getName());
            doctor.setSurname(newDoctorDto.getSurname());
            doctor.setLocations(List.of(location.get()));
            doctor.setSpecialization(specializationEntity);

            Doctor savedDoctor = doctorRepository.save(doctor);

            SpecializationDto specializationDto = new SpecializationDto(
                    specializationEntity.getSpecializationId(),
                    specializationEntity.getSpecializationName());
            return doctorMapper.toDoctorDto(savedDoctor, specializationDto);
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
        List<Doctor> allDoctors = doctorRepository.findAll();
        return allDoctors.stream()
                .map(doctorMapper::toDoctorDto)
                .collect(Collectors.toList());
    }

    public List<DoctorDto> getDoctorsBySpecialization(Long specializationId) {
        List<Doctor> doctors = doctorRepository.findBySpecialization_SpecializationId(specializationId);

        return doctors.stream()
                .map(doctor -> {
                    List<LocationDto> locationDtos = doctor.getLocations().stream().map(locationMapper::toLocationDto)
                            .toList();
                    return new DoctorDto(
                            doctor.getUserId(),
                            doctor.getName(),
                            doctor.getSurname(),
                            locationDtos,
                            SpecializationDto.from(doctor.getSpecialization()));
                })
                .collect(Collectors.toList());
    }

    public List<LocationDto> getLocationsByDoctorId(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isPresent()) {
            return doctor.get().getLocations().stream().map(locationMapper::toLocationDto).toList();
        } else {
            throw new MedHubServiceException("Not found");
        }
    }

    public List<DoctorDto> getDoctorsByCityAndSpecialization(String city, Long specializationId) {
        List<Doctor> doctors = doctorRepository.findByCityAndSpecialization(city, specializationId);

        return doctors.stream()
                .map(doctor -> {
                    List<LocationDto> locationDtos = doctor.getLocations().stream().map(locationMapper::toLocationDto)
                            .toList();
                    return new DoctorDto(
                            doctor.getUserId(),
                            doctor.getName(),
                            doctor.getSurname(),
                            locationDtos,
                            SpecializationDto.from(doctor.getSpecialization()));
                })
                .collect(Collectors.toList());
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
