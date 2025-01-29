package com.example.medhub.entity;

import com.example.medhub.dto.request.WorkerCreateRequestDTO;
import com.example.medhub.mapper.WorkerMapper;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "workers")
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkerEntity extends User {
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    LocationEntity location;

    public static WorkerEntity from(WorkerCreateRequestDTO workerCreateRequestDTO, String encryptedPassword) {
        return WorkerMapper.WORKER_MAPPER.toWorker(workerCreateRequestDTO, encryptedPassword);
    }
}
