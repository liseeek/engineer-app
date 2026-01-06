package com.example.medhub.dto;

import com.example.medhub.entity.SpecializationEntity;

public record SpecializationDto(Long specializationId, String specializationName) {
    public static SpecializationDto from(SpecializationEntity specializationEntity) {
        return new SpecializationDto(specializationEntity.getSpecializationId(), specializationEntity.getSpecializationName());
    }
}

