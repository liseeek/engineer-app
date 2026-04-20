package com.example.medhub.repository;

import com.example.medhub.entity.Doctor;
import com.example.medhub.enums.DoctorVerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends BaseUserRepository<Doctor>, JpaSpecificationExecutor<Doctor> {
    List<Doctor> findDistinctBySpecializations_SpecializationId(Long specializationId);

    Page<Doctor> findDistinctBySpecializations_SpecializationId(Long specializationId, Pageable pageable);

    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.locations l JOIN d.specializations s WHERE l.city = :city AND s.specializationId = :specializationId")
    List<Doctor> findByCityAndSpecialization(@Param("city") String city,
            @Param("specializationId") Long specializationId);

    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.locations l JOIN d.specializations s WHERE l.city = :city AND s.specializationId = :specializationId")
    Page<Doctor> findByCityAndSpecialization(@Param("city") String city,
            @Param("specializationId") Long specializationId, Pageable pageable);

    /**
     * Native SQL: JPQL {@code COUNT} on {@code Doctor} can produce invalid SQL with Hibernate 6
     * when combining {@code @SoftDelete} on {@link com.example.medhub.entity.User} with JOINED inheritance
     * (missing join for the {@code users.deleted} predicate).
     */
    @Query(value = "SELECT COUNT(*) FROM doctors d INNER JOIN users u ON u.user_id = d.user_id "
            + "WHERE d.pwz = :pwz AND u.deleted = false", nativeQuery = true)
    long countDoctorsWithPwz(@Param("pwz") String pwz);

    default boolean existsByPwz(String pwz) {
        return countDoctorsWithPwz(pwz) > 0;
    }

    List<Doctor> findByVerificationStatus(DoctorVerificationStatus status);

    Page<Doctor> findByVerificationStatus(DoctorVerificationStatus status, Pageable pageable);

    Page<Doctor> findByLocationsLocationId(Long locationId, Pageable pageable);
}
