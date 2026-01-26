package com.example.medhub.service;

import com.example.medhub.dto.LocationDto;
import com.example.medhub.dto.request.LocationCreateRequestDto;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.LocationMapper;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationsServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationsService locationsService;

    @Test
    void saveLocation_ShouldSave_WhenLocationDoesNotExist() {
        LocationCreateRequestDto request = new LocationCreateRequestDto();
        request.setLocationName("Clinic A");
        request.setCity("Warsaw");

        LocationEntity entity = new LocationEntity();
        entity.setLocationName("Clinic A");

        when(locationRepository.findLocationByLocationName("Clinic A")).thenReturn(Optional.empty());
        when(locationMapper.toLocationEntity(request)).thenReturn(entity);

        locationsService.saveLocation(request);

        verify(locationRepository).save(entity);
    }

    @Test
    void saveLocation_ShouldThrowException_WhenLocationExists() {
        LocationCreateRequestDto request = new LocationCreateRequestDto();
        request.setLocationName("Clinic A");

        when(locationRepository.findLocationByLocationName("Clinic A")).thenReturn(Optional.of(new LocationEntity()));

        assertThatThrownBy(() -> locationsService.saveLocation(request))
                .isInstanceOf(MedHubServiceException.class)
                .hasMessage("Already Exist");

        verify(locationRepository, never()).save(any());
    }

    @Test
    void deleteById_ShouldDelete_WhenLocationExists() {
        Long id = 1L;
        LocationEntity location = new LocationEntity();
        location.setLocationId(id);
        location.setDoctors(new ArrayList<>());

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        locationsService.deleteById(id);

        verify(doctorRepository).saveAll(any());
        verify(locationRepository).deleteById(id);
    }

    @Test
    void deleteById_ShouldThrowException_WhenLocationNotFound() {
        Long id = 1L;
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationsService.deleteById(id))
                .isInstanceOf(MedHubServiceException.class)
                .hasMessage("Not found");

        verify(locationRepository, never()).deleteById(any());
    }

    @Test
    void getLocations_ShouldReturnList() {
        LocationEntity loc1 = new LocationEntity();
        LocationDto dto1 = new LocationDto(1L, "Clinic A", "Street 1", "City", "Country");

        when(locationRepository.findAll()).thenReturn(List.of(loc1));
        when(locationMapper.toLocationDto(loc1)).thenReturn(dto1);

        List<LocationDto> result = locationsService.getLocations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).locationName()).isEqualTo("Clinic A");
    }
}
