package com.example.medhub.mapper;

import com.example.medhub.dto.request.WorkerCreateRequestDTO;
import com.example.medhub.entity.WorkerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class WorkerMapper {
    public static final WorkerMapper WORKER_MAPPER = Mappers.getMapper(WorkerMapper.class);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "password", source = "encryptedPassword")
    public abstract WorkerEntity toWorker(WorkerCreateRequestDTO workerCreateRequestDTO, String encryptedPassword);
}
