package com.example.medhub;

import com.example.medhub.dto.VisitNoteRequestDto;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.entity.Worker;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.enums.AppointmentType;
import com.example.medhub.enums.Authority;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.UserRepository;
import com.example.medhub.repository.VisitNoteRepository;
import com.example.medhub.repository.WorkerRepository;
import com.example.medhub.repository.SpecializationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DoctorOpsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AppointmentsRepository appointmentsRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private VisitNoteRepository visitNoteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpecializationRepository specializationRepository;

    private Doctor doctor;
    private AppointmentsEntity appointment;
    private Worker worker;
    private Worker otherWorker;

    @BeforeEach
    void setup() {
        LocationEntity location = new LocationEntity();
        location.setLocationName("Test Clinic");
        location.setCity("Test City");
        location.setAddress("Test St");
        location.setCountry("Poland");
        location.setPhoneNumber("123456789");
        locationRepository.save(location);

        LocationEntity otherLocation = new LocationEntity();
        otherLocation.setLocationName("Other Clinic");
        otherLocation.setCity("Other City");
        otherLocation.setAddress("Other St");
        otherLocation.setCountry("Poland");
        locationRepository.save(otherLocation);

        SpecializationEntity specialization = new SpecializationEntity();
        specialization.setSpecializationName("Test Cardiology");
        specializationRepository.save(specialization);

        doctor = new Doctor();
        doctor.setEmail("doc@test.com");
        doctor.setPassword("pass");
        doctor.setName("Doc");
        doctor.setSurname("Tor");
        doctor.setPhoneNumber("111");
        doctor.setPwz("9999999");
        doctor.setAuthority(Authority.ROLE_DOCTOR);
        doctor.setSpecialization(specialization);
        doctorRepository.save(doctor);

        appointment = new AppointmentsEntity();
        appointment.setDoctor(doctor);
        appointment.setLocation(location);
        appointment.setDate(LocalDateTime.now().plusHours(1).toLocalDate());
        appointment.setTime(LocalDateTime.now().plusHours(1).toLocalTime());
        appointment.setAppointmentStatus(AppointmentStatus.ACTIVE);
        appointment.setAppointmentType(AppointmentType.NFZ);
        appointmentsRepository.save(appointment);

        worker = new Worker();
        worker.setEmail("worker@test.com");
        worker.setPassword("pass");
        worker.setName("Work");
        worker.setSurname("Er");
        worker.setPhoneNumber("222");
        worker.setAuthority(Authority.ROLE_WORKER);
        worker.setLocation(location);
        workerRepository.save(worker);

        otherWorker = new Worker();
        otherWorker.setEmail("other@test.com");
        otherWorker.setPassword("pass");
        otherWorker.setName("Other");
        otherWorker.setSurname("Work");
        otherWorker.setPhoneNumber("333");
        otherWorker.setAuthority(Authority.ROLE_WORKER);
        otherWorker.setLocation(otherLocation);
        workerRepository.save(otherWorker);
    }

    @Test
    @WithMockUser(username = "doc@test.com", roles = "DOCTOR")
    void doctorCanSeeSchedule() throws Exception {
        mockMvc.perform(get("/api/doctor/appointments"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "doc@test.com", roles = "DOCTOR")
    void doctorCanConcludeVisit() throws Exception {
        VisitNoteRequestDto note = new VisitNoteRequestDto("Flu", "Rest", "Water");

        mockMvc.perform(post("/api/doctor/appointments/" + appointment.getAppointmentId() + "/note")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(note)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "worker@test.com", roles = "WORKER")
    void workerCanCancelAppointmentInSameLocation() throws Exception {
        mockMvc.perform(post("/api/facility/appointments/" + appointment.getAppointmentId() + "/cancel")
                .param("reason", "Emergency"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "other@test.com", roles = "WORKER")
    void workerCannotCancelAppointmentInDifferentLocation() throws Exception {
        mockMvc.perform(post("/api/facility/appointments/" + appointment.getAppointmentId() + "/cancel")
                .param("reason", "Malicious"))
                .andExpect(status().isForbidden());
    }
}
