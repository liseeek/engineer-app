package com.example.medhub.repository;

import com.example.medhub.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    Optional<LocationEntity> findLocationByLocationName(String locationName);
    Page<LocationEntity> findByLocationNameContainingIgnoreCase(String locationName, Pageable pageable);
}
