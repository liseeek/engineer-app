package com.example.medhub;

import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.enums.Authority;
import com.example.medhub.enums.DoctorVerificationStatus;
import com.example.medhub.repository.DoctorRepository;
import com.example.medhub.repository.LocationRepository;
import com.example.medhub.repository.SpecializationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DoctorVerificationFilterTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private SpecializationRepository specializationRepository;

    private SpecializationEntity specialization;
    private LocationEntity location;

    @BeforeEach
    void setup() {
        doctorRepository.deleteAll();
        specializationRepository.deleteAll();
        locationRepository.deleteAll();

        location = new LocationEntity();
        location.setLocationName("Filter Test Clinic");
        location.setCity("Warsaw");
        location.setAddress("Filter St 1");
        location.setCountry("Poland");
        location.setPhoneNumber("123456789");
        location = locationRepository.save(location);

        specialization = new SpecializationEntity();
        specialization.setSpecializationName("Cardiology-Filter-Test");
        specialization = specializationRepository.save(specialization);

        // Verified doctor
        Doctor verifiedDoc = new Doctor();
        verifiedDoc.setEmail("verified-filter@test.com");
        verifiedDoc.setPassword("Password123!");
        verifiedDoc.setName("Verified");
        verifiedDoc.setSurname("Doctor");
        verifiedDoc.setPwz("9991111");
        verifiedDoc.setPhoneNumber("111222333");
        verifiedDoc.setAuthority(Authority.ROLE_DOCTOR);
        verifiedDoc.setVerificationStatus(DoctorVerificationStatus.VERIFIED);
        verifiedDoc.setSpecializations(List.of(specialization));
        verifiedDoc.setLocations(List.of(location));
        doctorRepository.save(verifiedDoc);

        // Pending doctor
        Doctor pendingDoc = new Doctor();
        pendingDoc.setEmail("pending-filter@test.com");
        pendingDoc.setPassword("Password123!");
        pendingDoc.setName("Pending");
        pendingDoc.setSurname("Doctor");
        pendingDoc.setPwz("9991112");
        pendingDoc.setPhoneNumber("444555666");
        pendingDoc.setAuthority(Authority.ROLE_DOCTOR);
        pendingDoc.setVerificationStatus(DoctorVerificationStatus.PENDING);
        pendingDoc.setSpecializations(List.of(specialization));
        pendingDoc.setLocations(List.of(location));
        doctorRepository.save(pendingDoc);
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void searchDoctorsReturnsOnlyVerified() throws Exception {
        mockMvc.perform(get("/v1/doctors/search")
                .param("city", "Warsaw")
                .param("specializationId", specialization.getSpecializationId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Verified"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void getDoctorsByCityAndSpecializationReturnsOnlyVerified() throws Exception {
        mockMvc.perform(get("/v1/doctors/by-city-and-specialization")
                .param("city", "Warsaw")
                .param("specializationId", specialization.getSpecializationId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Verified"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void getDoctorsBySpecializationReturnsOnlyVerified() throws Exception {
        mockMvc.perform(get("/v1/doctors/by-specialization")
                .param("specializationId", specialization.getSpecializationId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Verified"));
    }
}
