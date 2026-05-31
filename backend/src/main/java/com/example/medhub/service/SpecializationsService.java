package com.example.medhub.service;

import com.example.medhub.dto.response.SpecializationDto;
import com.example.medhub.dto.request.SpecializationCreateRequestDto;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.SpecializationMapper;
import com.example.medhub.repository.SpecializationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecializationsService {
    private final SpecializationRepository specializationRepository;
    private final SpecializationMapper specializationMapper;

    public void saveSpecialization(SpecializationCreateRequestDto specializationCreateRequestDto) {
        if (specializationRepository
                .findSpecializationEntityBySpecializationName(specializationCreateRequestDto.getSpecializationName())
                .isPresent()) {
            throw new MedHubServiceException("Already Exist");
        }
        specializationRepository.save(specializationMapper.toEntity(specializationCreateRequestDto));
    }

    public List<SpecializationDto> getSpecializations(String search) {
        List<SpecializationEntity> specs;
        if (search != null && !search.isBlank()) {
            specs = specializationRepository.findBySpecializationNameContainingIgnoreCase(search.trim());
        } else {
            specs = specializationRepository.findAll();
        }
        return specs.stream()
                .map(specializationMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Cacheable(value = "specializations", key = "#city")
    public List<SpecializationDto> getSpecializationsByCity(String city) {
        return specializationRepository.findDistinctByDoctors_Locations_City(city).stream()
                .map(specializationMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSpecialization(Long id) {
        SpecializationEntity specialization = specializationRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Specialization not found"));

        if (specialization.getDoctors() != null && !specialization.getDoctors().isEmpty()) {
            throw new MedHubServiceException("Cannot delete specialization because it is assigned to doctors.");
        }

        specializationRepository.delete(specialization);
    }
}
