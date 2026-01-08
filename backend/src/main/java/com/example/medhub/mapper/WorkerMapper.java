package com.example.medhub.mapper;

import com.example.medhub.dto.request.WorkerCreateRequestDTO;
import com.example.medhub.entity.WorkerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface WorkerMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "password", source = "encryptedPassword")
    @Mapping(target = "authority", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    WorkerEntity toWorker(WorkerCreateRequestDTO workerCreateRequestDTO, String encryptedPassword);
}
