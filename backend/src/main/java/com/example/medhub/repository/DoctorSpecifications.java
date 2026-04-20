package com.example.medhub.repository;

import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.enums.DoctorVerificationStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public final class DoctorSpecifications {

    private DoctorSpecifications() {
    }

    public static Specification<Doctor> isVerified() {
        return (root, query, cb) ->
                cb.equal(root.get("verificationStatus"), DoctorVerificationStatus.VERIFIED);
    }

    public static Specification<Doctor> hasCity(String city) {
        return (root, query, cb) -> {
            if (city == null || city.isBlank()) {
                return cb.conjunction();
            }
            query.distinct(true);
            Join<Doctor, LocationEntity> locations = root.join("locations");
            return cb.equal(cb.lower(locations.get("city")), city.toLowerCase());
        };
    }

    public static Specification<Doctor> hasSpecialization(Long specializationId) {
        return (root, query, cb) -> {
            if (specializationId == null) {
                return cb.conjunction();
            }
            query.distinct(true);
            Join<Doctor, SpecializationEntity> specs = root.join("specializations");
            return cb.equal(specs.get("specializationId"), specializationId);
        };
    }

    public static Specification<Doctor> nameOrSurnameContains(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("surname")), pattern)
            );
        };
    }
}
