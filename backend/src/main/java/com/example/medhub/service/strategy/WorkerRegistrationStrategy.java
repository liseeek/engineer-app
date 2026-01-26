package com.example.medhub.service.strategy;

import com.example.medhub.dto.request.InvitationRegistrationRequestDto;
import com.example.medhub.entity.Invitation;
import com.example.medhub.entity.User;
import com.example.medhub.entity.Worker;
import com.example.medhub.enums.Authority;
import com.example.medhub.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkerRegistrationStrategy implements UserRegistrationStrategy {

    private final WorkerRepository workerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(String role) {
        return "WORKER".equalsIgnoreCase(role);
    }

    @Override
    public User register(InvitationRegistrationRequestDto request, Invitation invitation) {
        Worker worker = new Worker();

        worker.setEmail(invitation.getEmail());
        worker.setName(request.getName());
        worker.setSurname(request.getSurname());
        worker.setPassword(passwordEncoder.encode(request.getPassword()));
        worker.setPhoneNumber(request.getPhoneNumber());
        worker.setAuthority(Authority.ROLE_WORKER);
        worker.setLocation(invitation.getLocation());

        return workerRepository.save(worker);
    }
}
