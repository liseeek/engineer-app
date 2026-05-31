package com.example.medhub.service;

import com.example.medhub.dto.response.LocationDto;
import com.example.medhub.dto.request.LocationCreateRequestDto;
import com.example.medhub.dto.request.LocationUpdateRequestDto;
import com.example.medhub.entity.Admin;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.User;
import com.example.medhub.entity.Worker;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.exceptions.UnauthorizedOperationException;
import com.example.medhub.mapper.LocationMapper;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LocationsService {
    private final LocationRepository locationRepository;
    private final DoctorRepository doctorRepository;
    private final LocationMapper locationMapper;
    private final SecurityService securityService;

    public void saveLocation(LocationCreateRequestDto locationCreateRequestDto) {
        if (locationRepository.findLocationByLocationName(locationCreateRequestDto.getLocationName()).isPresent()){
            throw new MedHubServiceException("Already Exist");
        }
        locationRepository.save(locationMapper.toLocationEntity(locationCreateRequestDto));
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

    @Cacheable(value = "locations", key = "#search + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<LocationDto> getLocations(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return locationRepository.findByLocationNameContainingIgnoreCase(search, pageable)
                    .map(locationMapper::toLocationDto);
        }
        return locationRepository.findAll(pageable).map(locationMapper::toLocationDto);
    }

    public List<String> getDistinctLocations() {
        return locationRepository.findAll().stream()
                .map(LocationEntity::getCity)
                .distinct()
                .toList();
    }

    public LocationDto getById(Long id) {
        return locationRepository.findById(id)
                .map(locationMapper::toLocationDto)
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + id));
    }

    @Transactional
    public LocationDto updateLocation(Long id, LocationUpdateRequestDto dto) {
        LocationEntity location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + id));

        User current = securityService.getCurrentUser();
        boolean isAdmin = current instanceof Admin;
        boolean isWorkerHere = current instanceof Worker worker
                && worker.getLocation() != null
                && worker.getLocation().getLocationId().equals(id);

        if (!isAdmin && !isWorkerHere) {
            throw new UnauthorizedOperationException("You are not authorized to edit this facility.");
        }

        if (dto.getDescription() != null) {
            location.setDescription(dto.getDescription());
        }
        if (dto.getYearEstablished() != null) {
            location.setYearEstablished(dto.getYearEstablishedValidated());
        }
        if (dto.getPhoneNumber() != null) {
            location.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getEmail() != null) {
            location.setEmail(dto.getEmail());
        }

        return locationMapper.toLocationDto(locationRepository.save(location));
    }
}
