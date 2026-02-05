package com.example.medhub.service;

import com.example.medhub.dto.DoctorDto;
import com.example.medhub.dto.SpecializationDto;
import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.DoctorMapper;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.SpecializationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorsServiceTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private SpecializationRepository specializationRepository;
    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorsService doctorsService;

    @Test
    void saveDoctor_locationNotFound_throwsMedhubServiceException() {
        DoctorCreateRequestDto request = new DoctorCreateRequestDto();
        request.setLocationName("Nothing");

        when(locationRepository.findLocationByLocationName(any(String.class)))
                .thenReturn(Optional.empty());

        assertThrows(MedHubServiceException.class, () -> doctorsService.saveDoctor(request));

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void saveDoctor_success() {
        DoctorCreateRequestDto request = new DoctorCreateRequestDto();
        request.setLocationName("MaxMed");
        request.setSpecializationId(1L);
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
        savedDoctor.setSpecialization(specialization);
        savedDoctor.setLocations(List.of(location));

        DoctorDto doctorDto = new DoctorDto(10L, "Jan", "Kowalski", null, null);

        when(locationRepository.findLocationByLocationName(any())).thenReturn(Optional.of(location));
        when(specializationRepository.findById(1L)).thenReturn(Optional.of(specialization));
        when(doctorRepository.save(any())).thenReturn(savedDoctor);
        when(doctorMapper.toDoctorDto(any(Doctor.class), any(SpecializationDto.class))).thenReturn(doctorDto);

        DoctorDto result = doctorsService.saveDoctor(request);

        assertNotNull(result);
        assertEquals(10L, result.doctorId());
        assertEquals("Jan", result.name());
    }

    @Test
    void deleteById_doctorExists_success() {
        when(doctorRepository.existsById(any())).thenReturn(true);

        doctorsService.deleteById(1L);

        verify(doctorRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_doctorNotFound_throwsMedHubServiceException() {
        when(doctorRepository.existsById(any())).thenReturn(false);

        assertThrows(MedHubServiceException.class, () -> doctorsService.deleteById(1L));

        verify(doctorRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllDoctors_success() {
        Doctor doctor = new Doctor();
        doctor.setSurname("House");

        DoctorDto doctorDto = new DoctorDto(1L, "Gregory", "House", null, null);

        when(doctorRepository.findAll()).thenReturn(List.of(doctor));
        when(doctorMapper.toDoctorDto(doctor)).thenReturn(doctorDto);

        List<DoctorDto> result = doctorsService.getAllDoctors();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("House", result.get(0).surname());
        verify(doctorRepository, times(1)).findAll();
    }
}
