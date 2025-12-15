package com.example.medhub.service;

import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.request.LocationCreateRequestDto;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.LocationMapper;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LocationsService {
    private final LocationRepository locationRepository;
    private final DoctorRepository doctorRepository;

    public void saveLocation(LocationCreateRequestDto locationCreateRequestDto) {
        if (locationRepository.findLocationByLocationName(locationCreateRequestDto.getLocationName()).isPresent()){
            throw new MedHubServiceException("Already Exist");
        }
        locationRepository.save(LocationMapper.LOCATION_MAPPER.toLocationEntity(locationCreateRequestDto));
    }

    @Transactional
    public void deleteById(Long id){
        Optional<LocationEntity> optionalLocation = locationRepository.findById(id);

        if (optionalLocation.isPresent()){
            LocationEntity location = optionalLocation.get();
            location.getDoctors().forEach(doctor -> doctor.getLocations().remove(location));
            doctorRepository.saveAll(location.getDoctors());
            locationRepository.deleteById(id);
        } else {
            throw new MedHubServiceException("Not found");
        }
    }

    public List<LocationDto> getLocations() {
        return locationRepository.findAll().stream()
                .map(LocationDto::from)
                .toList();
    }

    public List<String> getDistinctLocations() {
        return locationRepository.findAll().stream()
                .map(LocationEntity::getCity)
                .distinct()
                .toList();
    }
}
