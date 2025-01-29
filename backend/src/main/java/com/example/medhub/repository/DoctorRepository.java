package com.example.medhub.repository;

import com.example.medhub.entity.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<DoctorEntity, Long> {
    List<DoctorEntity> findBySpecialization_SpecializationId(Long specializationId);
}
