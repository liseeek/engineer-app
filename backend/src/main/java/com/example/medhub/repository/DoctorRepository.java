package com.example.medhub.repository;

import com.example.medhub.entity.Doctor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends BaseUserRepository<Doctor> {
    List<Doctor> findBySpecialization_SpecializationId(Long specializationId);

    @Query("SELECT d FROM Doctor d JOIN d.locations l WHERE l.city = :city AND d.specialization.specializationId = :specializationId")
    List<Doctor> findByCityAndSpecialization(@Param("city") String city,
            @Param("specializationId") Long specializationId);

    boolean existsByPwz(String pwz);
}
