package com.example.medhub.service;

import com.example.medhub.dto.SpecializationDto;
import com.example.medhub.dto.request.SpecializationCreateRequestDto;
import com.example.medhub.entity.DoctorEntity;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.SpecializationMapper;
import com.example.medhub.repository.SpecializationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpecializationsService {
    private final SpecializationRepository specializationRepository;

    public void saveLocation(SpecializationCreateRequestDto specializationCreateRequestDto) {
        if (specializationRepository.findSpecializationEntityBySpecializationName(specializationCreateRequestDto.getSpecializationName()).isPresent()) {
            throw new MedHubServiceException("Already Exist");
        }
        specializationRepository.save(SpecializationEntity.from(specializationCreateRequestDto));
    }

    public List<SpecializationDto> getSpecializations() {
        return specializationRepository.findAll().stream()
                .map(SpecializationMapper.SPECIALIZATION_MAPPER::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<SpecializationDto> getSpecializationsByCity(String city) {
        return specializationRepository.findAll().stream()
                .filter(specializationEntity ->
                        specializationEntity.getDoctors().stream()
                                .map(DoctorEntity::getLocations)
                                .flatMap(Collection::stream)
                                .map(LocationEntity::getCity)
                                .collect(Collectors.toSet())
                                .contains(city)
                )
                .map(SpecializationMapper.SPECIALIZATION_MAPPER::entityToDto)
                .collect(Collectors.toList());
    }


}
