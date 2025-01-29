package com.example.medhub;

import com.example.medhub.entity.Authority;
import com.example.medhub.entity.UserEntity;
import com.example.medhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandLineAppStartupRunner implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${database.admin.name}")
    private String adminName;

    @Value("${database.admin.surname}")
    private String adminSurname;

    @Value("${database.admin.email}")
    private String adminEmail;

    @Value("${database.admin.phoneNumber}")
    private String adminPhoneNumber;

    @Value("${database.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findUserEntitiesByEmail(adminEmail).isEmpty()) {
            UserEntity admin = new UserEntity();
            admin.setName(adminName);
            admin.setSurname(adminSurname);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setPhoneNumber(adminPhoneNumber);
            admin.setAuthority(Authority.ROLE_ADMIN);
            userRepository.save(admin);
        }
    }
}