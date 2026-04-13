package com.example.medhub.repository;

import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.DoctorLocationRequest;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.enums.DoctorLocationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorLocationRequestRepository extends JpaRepository<DoctorLocationRequest, Long> {

    @Query("SELECT r FROM DoctorLocationRequest r JOIN FETCH r.location WHERE r.doctor = :doctor AND r.status = :status")
    List<DoctorLocationRequest> findByDoctorAndStatusWithLocation(
            @Param("doctor") Doctor doctor,
            @Param("status") DoctorLocationRequestStatus status);

    Optional<DoctorLocationRequest> findByDoctorAndLocationAndStatus(
            Doctor doctor,
            LocationEntity location,
            DoctorLocationRequestStatus status);
}
