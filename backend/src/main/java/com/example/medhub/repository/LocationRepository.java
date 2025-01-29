package com.example.medhub.repository;

import com.example.medhub.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    Optional<LocationEntity> findLocationByLocationName(String locationName);
}
