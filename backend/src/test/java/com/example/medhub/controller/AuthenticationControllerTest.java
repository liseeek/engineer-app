package com.example.medhub.controller;

import com.example.medhub.AbstractIntegrationTest;
import com.example.medhub.dto.request.AuthenticationRequest;
import com.example.medhub.dto.request.UserCreateRequestDto;
import com.example.medhub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.cache.type=none"
})
class AuthenticationControllerTest extends AbstractIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @BeforeEach
        void setup() {
                userRepository.deleteAll();
        }

        @AfterEach
        void cleanup() {
                userRepository.deleteAll();
        }

        @Test
        void shouldRegisterUserSuccessfully() throws Exception {
                UserCreateRequestDto request = new UserCreateRequestDto(
                                "Jan",
                                "Kowalski",
                                "jan.kowalski@example.com",
                                "Password123!",
                                "Password123!",
                                "123456789",
                                null);

                mockMvc.perform(post("/v1/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());
        }

        @Test
        void shouldFailRegistrationWithWeakPassword() throws Exception {
                UserCreateRequestDto request = new UserCreateRequestDto(
                                "Jan",
                                "Kowalski",
                                "jan.weak@example.com",
                                "weak",
                                "weak",
                                "123456789",
                                null);

                mockMvc.perform(post("/v1/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldFailRegistrationWithDuplicatePesel() throws Exception {
                UserCreateRequestDto firstUser = new UserCreateRequestDto(
                                "Jan", "Kowalski", "jan.unique@example.com", "Password123!", "Password123!",
                                "111222333", "99010112345");
                mockMvc.perform(post("/v1/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(firstUser)))
                                .andExpect(status().isCreated());

                UserCreateRequestDto secondUser = new UserCreateRequestDto(
                                "Anna", "Nowak", "anna.unique@example.com", "Password123!", "Password123!", "444555666",
                                "99010112345");

                mockMvc.perform(post("/v1/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(secondUser)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldLoginSuccessfully() throws Exception {
                UserCreateRequestDto registerRequest = new UserCreateRequestDto(
                                "Anna",
                                "Nowak",
                                "anna.nowak@example.com",
                                "Password123!",
                                "Password123!",
                                "987654321",
                                null);

                mockMvc.perform(post("/v1/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                AuthenticationRequest loginRequest = new AuthenticationRequest(
                                "anna.nowak@example.com",
                                "Password123!");

                mockMvc.perform(post("/v1/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.jwtToken", notNullValue()));
        }

        @Test
        void shouldFailLoginWithWrongPassword() throws Exception {
                UserCreateRequestDto registerRequest = new UserCreateRequestDto(
                                "Piotr",
                                "Zielinski",
                                "piotr.zielinski@example.com",
                                "Password123!",
                                "Password123!",
                                "123123123",
                                null);

                mockMvc.perform(post("/v1/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                AuthenticationRequest loginRequest = new AuthenticationRequest(
                                "piotr.zielinski@example.com",
                                "WrongPass1!");

                mockMvc.perform(post("/v1/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldLogoutAndBlacklistToken() throws Exception {
                UserCreateRequestDto registerRequest = new UserCreateRequestDto(
                                "Ewa",
                                "Kowalska",
                                "ewa.kowalska@example.com",
                                "Password123!",
                                "Password123!",
                                "555666777",
                                null);

                mockMvc.perform(post("/v1/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                AuthenticationRequest loginRequest = new AuthenticationRequest(
                                "ewa.kowalska@example.com",
                                "Password123!");

                String jwtToken = mockMvc.perform(post("/v1/signin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.jwtToken", notNullValue()))
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                String token = objectMapper.readTree(jwtToken).get("jwtToken").asText();

                mockMvc.perform(post("/v1/logout")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isNoContent());

                mockMvc.perform(get("/v1/doctors")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isForbidden());
        }
}
