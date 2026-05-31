package com.example.medhub.service;

import com.example.medhub.dto.response.DoctorDto;
import com.example.medhub.dto.response.SpecializationDto;
import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.DoctorMapper;
import com.example.medhub.mapper.LocationMapper;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.SpecializationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorCrudServiceTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private SpecializationRepository specializationRepository;
    @Mock
    private DoctorMapper doctorMapper;
    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private DoctorCrudService doctorCrudService;

    @Test
    void saveDoctor_locationNotFound_throwsMedhubServiceException() {
        DoctorCreateRequestDto request = new DoctorCreateRequestDto();
        request.setLocationName("Nothing");

        when(locationRepository.findLocationByLocationName(any(String.class)))
                .thenReturn(Optional.empty());

        assertThrows(MedHubServiceException.class, () -> doctorCrudService.saveDoctor(request));

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void saveDoctor_success() {
        DoctorCreateRequestDto request = new DoctorCreateRequestDto();
        request.setLocationName("MaxMed");
        request.setSpecializationIds(List.of(1L));
        request.setName("Jan");
        request.setSurname("Kowalski");

        LocationEntity location = new LocationEntity();
        location.setLocationName("MaxMed");

        SpecializationEntity specialization = new SpecializationEntity();
        specialization.setSpecializationId(1L);
        specialization.setSpecializationName("Cardiology");

        Doctor savedDoctor = new Doctor();
        savedDoctor.setUserId(10L);
        savedDoctor.setName("Jan");
        savedDoctor.setSurname("Kowalski");
        savedDoctor.setSpecializations(List.of(specialization));
        savedDoctor.setLocations(List.of(location));

        List<SpecializationDto> specDtos = List.of(new SpecializationDto(1L, "Cardiology"));
        DoctorDto doctorDto = new DoctorDto(10L, "Jan", "Kowalski", null, null, null, null, null, List.of(), specDtos);

        when(locationRepository.findLocationByLocationName(any())).thenReturn(Optional.of(location));
        when(specializationRepository.findById(1L)).thenReturn(Optional.of(specialization));
        when(doctorRepository.save(any())).thenReturn(savedDoctor);
        when(doctorMapper.toDoctorDto(any(Doctor.class))).thenReturn(doctorDto);

        DoctorDto result = doctorCrudService.saveDoctor(request);

        assertNotNull(result);
        assertEquals(10L, result.doctorId());
        assertEquals("Jan", result.name());
    }

    @Test
    void deleteById_doctorExists_success() {
        when(doctorRepository.existsById(any())).thenReturn(true);

        doctorCrudService.deleteById(1L);

        verify(doctorRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_doctorNotFound_throwsMedHubServiceException() {
        when(doctorRepository.existsById(any())).thenReturn(false);

        assertThrows(MedHubServiceException.class, () -> doctorCrudService.deleteById(1L));

        verify(doctorRepository, never()).deleteById(anyLong());
    }

    @Test
    void getDoctorById_found_returnsDto() {
        Doctor doctor = new Doctor();
        doctor.setUserId(5L);
        DoctorDto dto = new DoctorDto(5L, "Anna", "Nowak", null, null, null, null, null, List.of(), List.of());

        when(doctorRepository.findById(5L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toDoctorDto(doctor)).thenReturn(dto);

        DoctorDto result = doctorCrudService.getDoctorById(5L);

        assertThat(result.doctorId()).isEqualTo(5L);
    }

    @Test
    void getDoctorById_notFound_throwsEntityNotFoundException() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorCrudService.getDoctorById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchDoctors_returnsPageOfDtos() {
        Doctor doctor = new Doctor();
        doctor.setUserId(1L);
        DoctorDto dto = new DoctorDto(1L, "Greg", "House", null, null, null, null, null, List.of(), List.of());
        PageRequest pageable = PageRequest.of(0, 20);

        when(doctorRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(doctor)));
        when(doctorMapper.toDoctorDto(doctor)).thenReturn(dto);

        var result = doctorCrudService.searchDoctors("Warsaw", 1L, "Hou", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).surname()).isEqualTo("House");
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchDoctors_noFilters_returnsAllVerified() {
        when(doctorRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var result = doctorCrudService.searchDoctors(null, null, null, PageRequest.of(0, 20));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getAllDoctors_success() {
        Doctor doctor = new Doctor();
        doctor.setSurname("House");

        DoctorDto doctorDto = new DoctorDto(1L, "Gregory", "House", null, null, null, null, null, List.of(), List.of());

        when(doctorRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(doctor)));
        when(doctorMapper.toDoctorDto(doctor)).thenReturn(doctorDto);

        var result = doctorCrudService.getAllDoctors(PageRequest.of(0, 20), null);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("House", result.getContent().get(0).surname());
        verify(doctorRepository, times(1)).findAll(any(Pageable.class));
    }
}
