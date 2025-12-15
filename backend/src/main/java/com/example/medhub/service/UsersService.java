package com.example.medhub.service;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.dto.request.UserCreateRequestDto;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.Authority;
import com.example.medhub.entity.UserEntity;
import com.example.medhub.mapper.AppointmentsMapper;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.medhub.mapper.UserMapper;

// ... imports

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UserRepository userRepository;
    private final AppointmentsRepository appointmentsRepository;
    private final PasswordEncoder passwordEncoder;

    public void saveUser(UserCreateRequestDto newUser) {
        var encryptedPassword = passwordEncoder.encode(newUser.getPassword());
        UserEntity userEntity = UserMapper.USER_MAPPER.toUser(newUser, encryptedPassword);
        userEntity.setAuthority(Authority.ROLE_USER);
        userRepository.save(userEntity);
    }

    public List<AppointmentsDto> getAppointmentsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity user = userRepository.findUserEntitiesByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        Long userId = user.getUserId();
        List<AppointmentsEntity> appointments = appointmentsRepository.findByUserUserId(userId);
        return appointments.stream()
                .map(AppointmentsMapper.APPOINTMENTS_MAPPER::toAppointmentDto)
                .collect(Collectors.toList());
    }
}
