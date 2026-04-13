package com.example.medhub;

import com.example.medhub.dto.request.DoctorCreateRequestDto;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.SpecializationRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class UniquePwzValidationIntegrationTest extends AbstractIntegrationTest {

    /** Seed doctor John Doe (Liquibase change set 10). */
    private static final String SEEDED_TAKEN_PWZ = "1234567";
    private static final String FREE_PWZ = "9988776";

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private Validator validator;

    private LocationEntity location;
    private SpecializationEntity specialization;

    @BeforeEach
    void setUp() {
        location = locationRepository.findLocationByLocationName("City Health Clinic")
                .orElseThrow(() -> new IllegalStateException("Expected seeded location City Health Clinic"));
        specialization = specializationRepository.findSpecializationEntityBySpecializationName("Cardiology")
                .orElseThrow(() -> new IllegalStateException("Expected seeded specialization Cardiology"));
    }

    @Test
    void shouldRejectDoctorCreate_WhenPwzAlreadyExists() {
        DoctorCreateRequestDto dto = validDto(SEEDED_TAKEN_PWZ);

        Set<ConstraintViolation<DoctorCreateRequestDto>> violations = validator.validate(dto);

        Set<String> pwzMessages = violations.stream()
                .filter(v -> "pwz".equals(v.getPropertyPath().toString()))
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertThat(pwzMessages).contains("PWZ already exists");
    }

    @Test
    void shouldAllowDoctorCreate_WhenPwzIsUnique() {
        DoctorCreateRequestDto dto = validDto(FREE_PWZ);

        Set<ConstraintViolation<DoctorCreateRequestDto>> violations = validator.validate(dto);

        assertThat(violations.stream()
                .filter(v -> "pwz".equals(v.getPropertyPath().toString()))
                .map(ConstraintViolation::getMessage))
                .noneMatch(msg -> msg.contains("already exists"));
    }

    private DoctorCreateRequestDto validDto(String pwz) {
        DoctorCreateRequestDto dto = new DoctorCreateRequestDto();
        dto.setName("John");
        dto.setSurname("Smith");
        dto.setPwz(pwz);
        dto.setLocationName(location.getLocationName());
        dto.setSpecializationIds(List.of(specialization.getSpecializationId()));
        return dto;
    }
}
