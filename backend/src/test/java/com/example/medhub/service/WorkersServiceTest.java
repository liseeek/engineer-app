package com.example.medhub.service;

import com.example.medhub.dto.request.WorkerCreateRequestDTO;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.Worker;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.mapper.AppointmentsMapper;
import com.example.medhub.mapper.DoctorMapper;
import com.example.medhub.mapper.LocationMapper;
import com.example.medhub.mapper.WorkerMapper;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.WorkerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkersServiceTest {

    @Mock
    private WorkerRepository workerRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AppointmentsRepository appointmentsRepository;
    @Mock
    private DoctorMapper doctorMapper;
    @Mock
    private AppointmentsMapper appointmentsMapper;
    @Mock
    private WorkerMapper workerMapper;
    @Mock
    private LocationMapper locationMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SecurityService securityService;

    @InjectMocks
    private WorkersService workersService;

    @Test
    void saveWorker_ShouldSaveWorker_WhenLocationExists() {
        String locationName = "Gdańsk";
        WorkerCreateRequestDTO worker = new WorkerCreateRequestDTO();
        worker.setLocationName(locationName);
        worker.setPassword("secretPassword");
        worker.setPasswordConfirmation("secretPassword");
        worker.setName("Jan");
        worker.setSurname("Kowalski");
        worker.setEmail("jan@medhub.pl");
        worker.setPhoneNumber("123456789");

        LocationEntity location = new LocationEntity();
        location.setLocationName(locationName);

        Worker workerEntity = new Worker();
        workerEntity.setUserId(1L);
        workerEntity.setEmail(worker.getEmail());
        workerEntity.setLocation(location);

        when(locationRepository.findLocationByLocationName(locationName)).thenReturn(Optional.of(location));
        when(passwordEncoder.encode("secretPassword")).thenReturn("hashed_secret");
        when(workerMapper.toWorker(any(), any())).thenReturn(workerEntity);
        when(workerRepository.save(any(Worker.class))).thenReturn(workerEntity);
        when(securityService.getCurrentUserEmail()).thenReturn("admin@test.com");

        workersService.saveWorker(worker);

        verify(workerRepository, times(1)).save(any(Worker.class));
        verify(passwordEncoder, times(1)).encode("secretPassword");
    }

    @Test
    void saveWorker_ShouldThrowException_WhenLocationNotFound() {
        String locationName = "Narnia";
        WorkerCreateRequestDTO request = new WorkerCreateRequestDTO();
        request.setLocationName(locationName);

        when(locationRepository.findLocationByLocationName(locationName)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workersService.saveWorker(request))
                .isInstanceOf(MedHubServiceException.class)
                .hasMessage("Location not found");

        verify(workerRepository, never()).save(any(Worker.class));
    }
}