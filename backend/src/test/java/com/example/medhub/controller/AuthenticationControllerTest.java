package com.example.medhub.controller;

import com.example.medhub.AbstractIntegrationTest;
import com.example.medhub.dto.request.AuthenticationRequest;
import com.example.medhub.dto.request.UserCreateRequestDto;
import com.example.medhub.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthenticationControllerTest extends AbstractIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @AfterEach
        void cleanup() {
                // Clean up test-specific users by finding them first, then deleting by ID
                userRepository.findUserEntitiesByEmail("jan.kowalski@example.com")
                                .ifPresent(user -> userRepository.deleteById(user.getUserId()));
                userRepository.findUserEntitiesByEmail("jan.weak@example.com")
                                .ifPresent(user -> userRepository.deleteById(user.getUserId()));
                userRepository.findUserEntitiesByEmail("anna.nowak@example.com")
                                .ifPresent(user -> userRepository.deleteById(user.getUserId()));
                userRepository.findUserEntitiesByEmail("piotr.zielinski@example.com")
                                .ifPresent(user -> userRepository.deleteById(user.getUserId()));
        }

        @Test
        void shouldRegisterUserSuccessfully() throws Exception {
                UserCreateRequestDto request = new UserCreateRequestDto(
                                "Jan",
                                "Kowalski",
                                "jan.kowalski@example.com",
                                "StrongPass1!",
                                "StrongPass1!",
                                "123456789");

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
                                "123456789");

                mockMvc.perform(post("/v1/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldLoginSuccessfully() throws Exception {
                UserCreateRequestDto registerRequest = new UserCreateRequestDto(
                                "Anna",
                                "Nowak",
                                "anna.nowak@example.com",
                                "StrongPass1!",
                                "StrongPass1!",
                                "987654321");

                mockMvc.perform(post("/v1/users/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isCreated());

                AuthenticationRequest loginRequest = new AuthenticationRequest(
                                "anna.nowak@example.com",
                                "StrongPass1!");

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
                                "StrongPass1!",
                                "StrongPass1!",
                                "123123123");

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
}
