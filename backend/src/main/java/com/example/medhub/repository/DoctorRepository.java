package com.example.medhub.repository;

import com.example.medhub.entity.Doctor;
import com.example.medhub.enums.DoctorVerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends BaseUserRepository<Doctor> {
    List<Doctor> findDistinctBySpecializations_SpecializationId(Long specializationId);

    Page<Doctor> findDistinctBySpecializations_SpecializationId(Long specializationId, Pageable pageable);

    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.locations l JOIN d.specializations s WHERE l.city = :city AND s.specializationId = :specializationId")
    List<Doctor> findByCityAndSpecialization(@Param("city") String city,
            @Param("specializationId") Long specializationId);

    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.locations l JOIN d.specializations s WHERE l.city = :city AND s.specializationId = :specializationId")
    Page<Doctor> findByCityAndSpecialization(@Param("city") String city,
            @Param("specializationId") Long specializationId, Pageable pageable);

    /**
     * Explicit JPQL (see {@link UserRepository#countUsersWithEmail(String)} for rationale).
     */
    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.pwz = :pwz")
    long countDoctorsWithPwz(@Param("pwz") String pwz);

    default boolean existsByPwz(String pwz) {
        return countDoctorsWithPwz(pwz) > 0;
    }

    List<Doctor> findByVerificationStatus(DoctorVerificationStatus status);

    Page<Doctor> findByVerificationStatus(DoctorVerificationStatus status, Pageable pageable);

    Page<Doctor> findByLocationsLocationId(Long locationId, Pageable pageable);
}
