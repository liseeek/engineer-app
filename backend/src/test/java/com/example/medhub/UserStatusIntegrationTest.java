package com.example.medhub;

import com.example.medhub.entity.User;
import com.example.medhub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "medhub.security.encryption.key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=",
        "medhub.security.hashing.salt=MTIzNDU2Nzg5MDEyMzQ1Ng==",
        "jwt.secret.key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNA==",
        "ADMIN_PASSWORD=admin",
        "DEMO_PASSWORD=demo"
})
@Transactional
public class UserStatusIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldToggleUserStatusAndBlockLogin() throws Exception {
        // 1. Setup user
        User user = new com.example.medhub.entity.Patient();
        user.setEmail("test-lock@medhub.com");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setName("Test");
        user.setSurname("Lock");
        user.setPhoneNumber("123456789");
        user.setAuthority(com.example.medhub.enums.Authority.ROLE_PATIENT);
        user.setLocked(false);
        user = userRepository.saveAndFlush(user);
        
        Long userId = user.getUserId();

        // 2. Toggle status to LOCKED
        mockMvc.perform(patch("/v1/users/{id}/status", userId))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertTrue(updatedUser.isLocked(), "User should be locked after patch");

        // 3. Attempt login - should fail because account is locked
        String loginJson = """
                {
                    "email": "test-lock@medhub.com",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/v1/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized());

        // 4. Toggle status back to UNLOCKED
        mockMvc.perform(patch("/v1/users/{id}/status", userId))
                .andExpect(status().isOk());

        updatedUser = userRepository.findById(userId).orElseThrow();
        assertFalse(updatedUser.isLocked(), "User should be unlocked after second patch");

        // 5. Attempt login - should succeed
        mockMvc.perform(post("/v1/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk());
    }
}
