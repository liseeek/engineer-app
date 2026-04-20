package com.example.medhub.service;

import com.example.medhub.dto.response.LocationDto;
import com.example.medhub.dto.request.LocationCreateRequestDto;
import com.example.medhub.dto.request.LocationUpdateRequestDto;
import com.example.medhub.entity.Admin;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Worker;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.exceptions.UnauthorizedOperationException;
import com.example.medhub.mapper.LocationMapper;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    @Mock
    private SecurityService securityService;

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
        LocationDto dto1 = new LocationDto(1L, "Clinic A", "Street 1", "City", "Country", null, null, null, null);

        when(locationRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(loc1)));
        when(locationMapper.toLocationDto(loc1)).thenReturn(dto1);

        var result = locationsService.getLocations(PageRequest.of(0, 50));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).locationName()).isEqualTo("Clinic A");
    }

    @Test
    void getById_ShouldReturnDto_WhenLocationExists() {
        LocationEntity loc = new LocationEntity();
        loc.setLocationId(3L);
        LocationDto dto = new LocationDto(3L, "MedCare", "Main St", "Krakow", "PL", null, null, "Great clinic", 2005);

        when(locationRepository.findById(3L)).thenReturn(Optional.of(loc));
        when(locationMapper.toLocationDto(loc)).thenReturn(dto);

        LocationDto result = locationsService.getById(3L);

        assertThat(result.locationId()).isEqualTo(3L);
        assertThat(result.description()).isEqualTo("Great clinic");
    }

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        when(locationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationsService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateLocation_Admin_Succeeds() {
        LocationEntity loc = new LocationEntity();
        loc.setLocationId(1L);
        Admin admin = new Admin();

        LocationUpdateRequestDto dto = new LocationUpdateRequestDto();
        dto.setDescription("New desc");

        LocationDto resultDto = new LocationDto(1L, "Clinic", "St", "City", "PL", null, null, "New desc", null);

        when(locationRepository.findById(1L)).thenReturn(Optional.of(loc));
        when(securityService.getCurrentUser()).thenReturn(admin);
        when(locationRepository.save(loc)).thenReturn(loc);
        when(locationMapper.toLocationDto(loc)).thenReturn(resultDto);

        LocationDto result = locationsService.updateLocation(1L, dto);

        assertThat(result.description()).isEqualTo("New desc");
    }

    @Test
    void updateLocation_WorkerAtSameLocation_Succeeds() {
        LocationEntity loc = new LocationEntity();
        loc.setLocationId(2L);

        Worker worker = new Worker();
        worker.setLocation(loc);

        LocationUpdateRequestDto dto = new LocationUpdateRequestDto();
        dto.setYearEstablished(2010);

        LocationDto resultDto = new LocationDto(2L, "Clinic", "St", "City", "PL", null, null, null, 2010);

        when(locationRepository.findById(2L)).thenReturn(Optional.of(loc));
        when(securityService.getCurrentUser()).thenReturn(worker);
        when(locationRepository.save(loc)).thenReturn(loc);
        when(locationMapper.toLocationDto(loc)).thenReturn(resultDto);

        LocationDto result = locationsService.updateLocation(2L, dto);

        assertThat(result.yearEstablished()).isEqualTo(2010);
    }

    @Test
    void updateLocation_WorkerAtDifferentLocation_ThrowsUnauthorized() {
        LocationEntity loc = new LocationEntity();
        loc.setLocationId(3L);

        LocationEntity workerLoc = new LocationEntity();
        workerLoc.setLocationId(99L);

        Worker worker = new Worker();
        worker.setLocation(workerLoc);

        when(locationRepository.findById(3L)).thenReturn(Optional.of(loc));
        when(securityService.getCurrentUser()).thenReturn(worker);

        assertThatThrownBy(() -> locationsService.updateLocation(3L, new LocationUpdateRequestDto()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }
}
