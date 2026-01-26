package com.example.medhub;

import com.example.medhub.dto.request.UserCreateRequestDto;
import com.example.medhub.dto.request.WorkerCreateRequestDTO;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Patient;
import com.example.medhub.entity.Worker;
import com.example.medhub.enums.Authority;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.UserRepository;
import com.example.medhub.repository.WorkerRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class UniqueEmailValidationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private Validator validator;

    private Patient existingUser;
    private Worker existingWorker;
    private LocationEntity testLocation;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        workerRepository.deleteAll();

        testLocation = locationRepository.findLocationByLocationName("Test Hospital")
                .orElseGet(() -> {
                    LocationEntity newLocation = new LocationEntity();
                    newLocation.setLocationName("Test Hospital");
                    newLocation.setAddress("123 Test St");
                    newLocation.setCity("Test City");
                    newLocation.setCountry("Test Country");
                    return locationRepository.save(newLocation);
                });

        existingUser = new Patient();
        existingUser.setName("Jan");
        existingUser.setSurname("Kowalski");
        existingUser.setEmail("user@test.com");
        existingUser.setPassword("hashedPassword123");
        existingUser.setPhoneNumber("111222333");
        existingUser.setPesel("12345678901");
        existingUser.setAuthority(Authority.ROLE_PATIENT);
        existingUser = userRepository.save(existingUser);

        existingWorker = new Worker();
        existingWorker.setName("Anna");
        existingWorker.setSurname("Nowak");
        existingWorker.setEmail("worker@test.com");
        existingWorker.setPassword("hashedPassword456");
        existingWorker.setPhoneNumber("444555666");
        existingWorker.setAuthority(Authority.ROLE_WORKER);
        existingWorker.setLocation(testLocation);
        existingWorker = workerRepository.save(existingWorker);
    }

    @Test
    void shouldRejectUserRegistration_WhenEmailBelongsToWorker() {
        UserCreateRequestDto userDto = new UserCreateRequestDto();
        userDto.setName("Piotr");
        userDto.setSurname("Testowy");
        userDto.setEmail("worker@test.com");
        userDto.setPassword("Password1$");
        userDto.setPasswordConfirmation("Password1$");
        userDto.setPhoneNumber("999888777");

        Set<ConstraintViolation<UserCreateRequestDto>> violations = validator.validate(userDto);

        assertThat(violations).hasSize(1);
        ConstraintViolation<UserCreateRequestDto> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Email already exists");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    void shouldRejectWorkerRegistration_WhenEmailBelongsToUser() {
        WorkerCreateRequestDTO workerDto = new WorkerCreateRequestDTO();
        workerDto.setName("Maria");
        workerDto.setSurname("Testowa");
        workerDto.setEmail("user@test.com");
        workerDto.setPassword("Password1$");
        workerDto.setPasswordConfirmation("Password1$");
        workerDto.setPhoneNumber("777666555");
        workerDto.setLocationName("Test Hospital");

        Set<ConstraintViolation<WorkerCreateRequestDTO>> violations = validator.validate(workerDto);

        assertThat(violations).hasSize(1);
        ConstraintViolation<WorkerCreateRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo("Email already exists");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    void shouldAllowUserRegistration_WhenEmailIsUnique() {
        UserCreateRequestDto userDto = new UserCreateRequestDto();
        userDto.setName("Unique");
        userDto.setSurname("User");
        userDto.setEmail("unique@test.com");
        userDto.setPassword("Password1$");
        userDto.setPasswordConfirmation("Password1$");
        userDto.setPhoneNumber("123123123");

        Set<ConstraintViolation<UserCreateRequestDto>> violations = validator.validate(userDto);

        assertThat(violations).isEmpty();
    }
}
