package com.example.medhub.repository;

import com.example.medhub.entity.AppointmentType;
import com.example.medhub.entity.AppointmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentsRepository extends JpaRepository<AppointmentsEntity, Long> {
    List<AppointmentsEntity> findByUserUserId(Long userId);

    @Query("SELECT a FROM AppointmentsEntity a " +
            "WHERE a.location.locationId = :locationId " +
            "AND a.doctor.doctorId = :doctorId " +
            "AND a.appointmentType = :appointmentType")
    List<AppointmentsEntity> findAppointmentsByFilters(
            @Param("locationId") String locationId,
            @Param("doctorId") String doctorId,
            @Param("appointmentType") AppointmentType appointmentType);
}