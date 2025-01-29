package com.example.medhub.dto;

import com.example.medhub.entity.SpecializationEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SpecializationDto {
    private final Long specializationId;
    private final String specializationName;

    public static SpecializationDto from(SpecializationEntity specializationEntity) {
        return new SpecializationDto(specializationEntity.getSpecializationId(), specializationEntity.getSpecializationName());
    }
}

