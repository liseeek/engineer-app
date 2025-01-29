package com.example.medhub.repository;

import com.example.medhub.entity.WorkerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<WorkerEntity, Long> {
    Optional<WorkerEntity> findWorkerEntitiesByEmail(String email);
}
