package com.example.medhub.service;

import com.example.medhub.entity.Admin;
import com.example.medhub.entity.Doctor;
import com.example.medhub.entity.Patient;
import com.example.medhub.entity.User;
import com.example.medhub.entity.Worker;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;

    public Patient getCurrentPatient() {
        User user = getCurrentUser();
        if (user instanceof Patient patient) {
            return patient;
        }
        throw new MedHubServiceException("Current user is not a Patient");
    }

    public Worker getCurrentWorker() {
        User user = getCurrentUser();
        if (user instanceof Worker worker) {
            return worker;
        }
        throw new MedHubServiceException("Current user is not a Worker");
    }

    public Doctor getCurrentDoctor() {
        User user = getCurrentUser();
        if (user instanceof Doctor doctor) {
            return doctor;
        }
        throw new MedHubServiceException("Current user is not a Doctor");
    }

    public Admin getCurrentAdmin() {
        User user = getCurrentUser();
        if (user instanceof Admin admin) {
            return admin;
        }
        throw new MedHubServiceException("Current user is not an Admin");
    }

    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        if ("SYSTEM".equals(email) || "anonymousUser".equals(email)) {
            throw new MedHubServiceException("User not authenticated");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new MedHubServiceException("User not found: " + email));
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymousUser";
        }
        return authentication.getName();
    }
}
