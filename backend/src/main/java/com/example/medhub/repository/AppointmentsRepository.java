package com.example.medhub.repository;

import com.example.medhub.enums.AppointmentType;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.LocationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppointmentsRepository extends JpaRepository<AppointmentsEntity, Long> {
        List<AppointmentsEntity> findByLocation(LocationEntity location);

        List<AppointmentsEntity> findByUserUserId(Long userId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT a FROM AppointmentsEntity a WHERE a.appointmentId = :id")
        Optional<AppointmentsEntity> findWithLockingById(@Param("id") Long id);

        @Query("SELECT a FROM AppointmentsEntity a " +
                        "WHERE a.location.locationId = :locationId " +
                        "AND a.doctor.doctorId = :doctorId " +
                        "AND a.appointmentType = :appointmentType")
        List<AppointmentsEntity> findAppointmentsByFilters(
                        @Param("locationId") Long locationId,
                        @Param("doctorId") Long doctorId,
                        @Param("appointmentType") AppointmentType appointmentType);

        @Query("SELECT DISTINCT a FROM AppointmentsEntity a " +
                        "JOIN FETCH a.doctor d " +
                        "JOIN FETCH d.specialization " +
                        "JOIN FETCH a.user u " +
                        "LEFT JOIN FETCH d.locations " +
                        "WHERE a.location = :location " +
                        "AND a.user IS NOT NULL")
        List<AppointmentsEntity> findAllScheduledByLocation(@Param("location") LocationEntity location);
}