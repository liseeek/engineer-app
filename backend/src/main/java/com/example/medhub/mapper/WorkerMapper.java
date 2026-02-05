package com.example.medhub.mapper;

import com.example.medhub.dto.request.WorkerCreateRequestDTO;
import com.example.medhub.entity.Worker;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WorkerMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "password", source = "encryptedPassword")
    @Mapping(target = "authority", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    Worker toWorker(WorkerCreateRequestDTO workerCreateRequestDTO, String encryptedPassword);
}
