package com.example.medhub.service;

import com.example.medhub.dto.SpecializationDto;
import com.example.medhub.dto.request.SpecializationCreateRequestDto;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.SpecializationMapper;
import com.example.medhub.repository.SpecializationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecializationsService {
    private final SpecializationRepository specializationRepository;
    private final SpecializationMapper specializationMapper;

    public void saveLocation(SpecializationCreateRequestDto specializationCreateRequestDto) {
        if (specializationRepository.findSpecializationEntityBySpecializationName(specializationCreateRequestDto.getSpecializationName()).isPresent()) {
            throw new MedHubServiceException("Already Exist");
        }
        specializationRepository.save(specializationMapper.toEntity(specializationCreateRequestDto));
    }

    public List<SpecializationDto> getSpecializations() {
        return specializationRepository.findAll().stream()
                .map(specializationMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<SpecializationDto> getSpecializationsByCity(String city) {
        return specializationRepository.findDistinctByDoctors_Locations_City(city).stream()
                .map(specializationMapper::entityToDto)
                .collect(Collectors.toList());
    }


}
