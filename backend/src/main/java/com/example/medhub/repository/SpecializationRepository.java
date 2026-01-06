package com.example.medhub.repository;

import com.example.medhub.entity.SpecializationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpecializationRepository extends JpaRepository<SpecializationEntity, Long> {
    Optional<SpecializationEntity> findSpecializationEntityBySpecializationName(String specializationName);

    List<SpecializationEntity> findDistinctByDoctors_Locations_City(String city);
}
