package com.example.medhub.repository;

import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.enums.AppointmentType;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.LocationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppointmentsRepository extends JpaRepository<AppointmentsEntity, Long> {
        List<AppointmentsEntity> findByLocation(LocationEntity location);

        List<AppointmentsEntity> findByPatientUserId(Long userId);

        @Query("SELECT a FROM AppointmentsEntity a " +
                        "JOIN FETCH a.patient p " +
                        "JOIN FETCH a.location l " +
                        "WHERE a.doctor.userId = :doctorId " +
                        "ORDER BY a.date ASC, a.time ASC")
        List<AppointmentsEntity> findAllByDoctorUserId(@Param("doctorId") Long doctorId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT a FROM AppointmentsEntity a WHERE a.appointmentId = :id")
        Optional<AppointmentsEntity> findWithLockingById(@Param("id") Long id);

        @Query("SELECT a FROM AppointmentsEntity a " +
                        "WHERE a.location.locationId = :locationId " +
                        "AND a.doctor.userId = :doctorId " +
                        "AND a.appointmentType = :appointmentType")
        List<AppointmentsEntity> findAppointmentsByFilters(
                        @Param("locationId") Long locationId,
                        @Param("doctorId") Long doctorId,
                        @Param("appointmentType") AppointmentType appointmentType);

        @Query("SELECT DISTINCT a FROM AppointmentsEntity a " +
                        "JOIN FETCH a.doctor d " +
                        "JOIN FETCH a.patient u " +
                        "LEFT JOIN FETCH d.locations " +
                        "WHERE a.location = :location " +
                        "AND a.patient IS NOT NULL")
        List<AppointmentsEntity> findAllScheduledByLocation(@Param("location") LocationEntity location);

        Page<AppointmentsEntity> findAllByLocationAndPatientIsNotNullOrderByDateAscTimeAsc(
                        LocationEntity location, Pageable pageable);

        @Query("SELECT COUNT(a) FROM AppointmentsEntity a WHERE a.patient.userId = :patientUserId "
                        + "AND a.date >= :minDate AND a.appointmentStatus IN :statuses")
        long countUpcomingForPatient(
                        @Param("patientUserId") Long patientUserId,
                        @Param("minDate") LocalDate minDate,
                        @Param("statuses") Collection<AppointmentStatus> statuses);
}
