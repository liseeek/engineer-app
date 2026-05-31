package com.example.medhub;
 
import com.example.medhub.dto.request.CreateDoctorLocationRequestDto;
import com.example.medhub.dto.request.DoctorSignupRequestDto;
import com.example.medhub.dto.request.InvitationRegistrationRequestDto;
import com.example.medhub.dto.request.InvitationRequestDto;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.Invitation;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.entity.Worker;
import com.example.medhub.enums.Authority;
import com.example.medhub.enums.DoctorVerificationStatus;
import com.example.medhub.enums.InvitationStatus;
import com.example.medhub.repository.DoctorLocationRequestRepository;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.InvitationRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.SpecializationRepository;
import com.example.medhub.repository.WorkerRepository;
import com.example.medhub.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DoctorOnboardingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private SpecializationRepository specializationRepository;
    @Autowired
    private InvitationRepository invitationRepository;
    @Autowired
    private WorkerRepository workerRepository;
    @Autowired
    private DoctorLocationRequestRepository doctorLocationRequestRepository;
    @Autowired
    private ObjectMapper objectMapper;
 
    private SpecializationEntity spec;
    private LocationEntity loc;
 
    @BeforeEach
    void setup() {
        doctorRepository.deleteAll();
        invitationRepository.deleteAll();
        specializationRepository.deleteAll();
        locationRepository.deleteAll();

        spec = new SpecializationEntity();
        spec.setSpecializationName("Onboarding Cardiology");
        spec = specializationRepository.save(spec);
 
        loc = new LocationEntity();
        loc.setLocationName("Onboarding Clinic");
        loc.setCity("Warsaw");
        loc.setAddress("Warszawska 1");
        loc.setPhoneNumber("123123123");
        loc.setCountry("Poland");
        loc = locationRepository.save(loc);
    }
 
    @Test
    void testDoctorSelfSignupResultsInPendingStatus() throws Exception {
        DoctorSignupRequestDto request = DoctorSignupRequestDto.builder()
                .email("newdoc-onboard@test.com")
                .password("Password123!")
                .passwordConfirmation("Password123!")
                .name("John")
                .surname("Doe")
                .pwz("1112221")
                .phoneNumber("555666777")
                .specializationIds(List.of(spec.getSpecializationId()))
                .build();
 
        mockMvc.perform(post("/v1/doctors/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
 
        Doctor doctor = doctorRepository.findByEmail("newdoc-onboard@test.com")
                .orElseThrow();
        assertThat(doctor.getVerificationStatus()).isEqualTo(DoctorVerificationStatus.PENDING);
    }
 
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminCanVerifyDoctor() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setEmail("pending@test.com");
        doctor.setPassword("Password123!");
        doctor.setName("Pending");
        doctor.setSurname("Doc");
        doctor.setPwz("2222222");
        doctor.setPhoneNumber("123456789");
        doctor.setVerificationStatus(DoctorVerificationStatus.PENDING);
        doctor.setAuthority(Authority.ROLE_DOCTOR);
        doctorRepository.save(doctor);
 
        mockMvc.perform(patch("/v1/doctors/" + doctor.getUserId() + "/verify")
                        .param("status", "VERIFIED"))
                .andExpect(status().isNoContent());
 
        Doctor updated = doctorRepository.findById(doctor.getUserId()).orElseThrow();
        assertThat(updated.getVerificationStatus()).isEqualTo(DoctorVerificationStatus.VERIFIED);
    }
 
    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminCanInviteDoctorAndItResultsInVerifiedStatus() throws Exception {
        // 1. Send Invitation
        InvitationRequestDto inviteRequest = InvitationRequestDto.builder()
                .email("invited-onboard@test.com")
                .role("DOCTOR")
                .locationId(loc.getLocationId())
                .pwz("1112223")
                .specializationId(spec.getSpecializationId())
                .build();
 
        mockMvc.perform(post("/v1/admin/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isAccepted());
 
        Invitation invitation = invitationRepository.findAll().stream()
                .filter(i -> i.getEmail().equals("invited-onboard@test.com"))
                .findFirst().orElseThrow();
 
        // 2. Register with invitation
        InvitationRegistrationRequestDto regRequest = InvitationRegistrationRequestDto.builder()
                .token(invitation.getToken())
                .name("Invited")
                .surname("Doctor")
                .password("Password123!")
                .passwordConfirmation("Password123!")
                .phoneNumber("999888777")
                .pwz("1112223")
                .build();
 
        mockMvc.perform(post("/v1/invitations/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());
 
        Doctor doctor = doctorRepository.findByEmail("invited-onboard@test.com").orElseThrow();
        assertThat(doctor.getVerificationStatus()).isEqualTo(DoctorVerificationStatus.VERIFIED);
        assertThat(doctor.getLocations()).contains(loc);
        assertThat(doctor.getSpecializations()).contains(spec);
    }
 
    @Test
    void testWorkerCanRequestDoctorAndDoctorCanAccept() throws Exception {
        // Setup Verified Doctor
        Doctor doctor = new Doctor();
        doctor.setEmail("handshake-onboard@test.com");
        doctor.setPassword("Password123!");
        doctor.setName("Hand");
        doctor.setSurname("Shake");
        doctor.setPwz("1112224");
        doctor.setPhoneNumber("123456789");
        doctor.setVerificationStatus(DoctorVerificationStatus.VERIFIED);
        doctor.setAuthority(Authority.ROLE_DOCTOR);
        doctorRepository.save(doctor);
 
        // Setup Worker
        Worker worker = new Worker();
        worker.setEmail("worker@test.com");
        worker.setPassword("pass");
        worker.setName("Work");
        worker.setSurname("Er");
        worker.setPhoneNumber("123456789");
        worker.setAuthority(Authority.ROLE_WORKER);
        worker.setLocation(loc);
        workerRepository.save(worker);
 
        // 1. Worker sends request
        CreateDoctorLocationRequestDto reqDto = new CreateDoctorLocationRequestDto();
        reqDto.setDoctorId(doctor.getUserId());
 
        mockMvc.perform(post("/v1/workers/doctor-location-requests")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("worker@test.com").roles("WORKER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated());
 
        var pendingRequests = doctorLocationRequestRepository.findAll();
        var request = pendingRequests.stream()
                .filter(r -> r.getDoctor().getUserId().equals(doctor.getUserId()))
                .findFirst().orElseThrow();
 
        // 2. Doctor accepts
        mockMvc.perform(post("/v1/doctor/location-requests/" + request.getId() + "/accept")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("handshake-onboard@test.com").roles("DOCTOR")))
                .andExpect(status().isOk());
 
        Doctor updatedDoctor = doctorRepository.findById(doctor.getUserId()).orElseThrow();
        assertThat(updatedDoctor.getLocations()).contains(loc);
    }
}
