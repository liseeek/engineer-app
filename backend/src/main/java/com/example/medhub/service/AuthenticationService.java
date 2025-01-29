package com.example.medhub.service;


import com.example.medhub.entity.User;
import com.example.medhub.entity.UserEntity;
import com.example.medhub.entity.WorkerEntity;
import com.example.medhub.repository.UserRepository;
import com.example.medhub.repository.WorkerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.util.Optional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AuthenticationService implements UserDetailsService {
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;

    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
        return findByEmailOrThrow(email);
    }

    private User findByEmailOrThrow(String email) {
        Optional<UserEntity> user = userRepository.findUserEntitiesByEmail(email);
        if (user.isPresent()) {
            return user.get();
        }
        Optional<WorkerEntity> worker = workerRepository.findWorkerEntitiesByEmail(email);
        if (worker.isPresent()) {
            return worker.get();
        }
        throw new NotFoundException("User with email=%s not found".formatted(email));
    }
}
