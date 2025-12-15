package com.example.medhub.repository;

import com.example.medhub.entity.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends JpaRepository<DoctorEntity, Long> {
    List<DoctorEntity> findBySpecialization_SpecializationId(Long specializationId);

    @Query("SELECT d FROM DoctorEntity d JOIN d.locations l WHERE l.city = :city AND d.specialization.specializationId = :specializationId")
    List<DoctorEntity> findByCityAndSpecialization(@Param("city") String city, @Param("specializationId") Long specializationId);
}
