package com.example.medhub.repository;

import com.example.medhub.entity.SpecializationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpecializationRepository extends JpaRepository<SpecializationEntity, Long> {
    Optional<SpecializationEntity> findSpecializationEntityBySpecializationName(String specializationName);

    /**
     * Native SQL: Spring Data derived query {@code findDistinctByDoctors_Locations_City} can produce invalid SQL
     * with Hibernate 6 when combining {@code @SoftDelete} on {@link com.example.medhub.entity.User} with JOINED
     * inheritance (missing join for the {@code users.deleted} predicate).
     */
    @Query(value = "SELECT DISTINCT s.* FROM specializations s "
            + "INNER JOIN doctor_specializations ds ON s.specialization_id = ds.specialization_id "
            + "INNER JOIN users u ON ds.user_id = u.user_id AND u.deleted = false "
            + "INNER JOIN doctor_locations dl ON ds.user_id = dl.user_id "
            + "INNER JOIN locations l ON dl.location_id = l.location_id "
            + "WHERE l.city = :city", nativeQuery = true)
    List<SpecializationEntity> findDistinctByDoctors_Locations_City(@Param("city") String city);

    List<SpecializationEntity> findBySpecializationNameContainingIgnoreCase(String specializationName);
}
