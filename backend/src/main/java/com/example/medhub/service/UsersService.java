package com.example.medhub.service;

import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.dto.request.UserCreateRequestDto;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.enums.Authority;
import com.example.medhub.entity.Patient;
import com.example.medhub.entity.User;
import com.example.medhub.mapper.AppointmentsMapper;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.medhub.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UserRepository userRepository;
    private final AppointmentsRepository appointmentsRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AppointmentsMapper appointmentsMapper;
    private final SecurityService securityService;
    private final CryptoService cryptoService;

    public void saveUser(UserCreateRequestDto newUser) {
        var encryptedPassword = passwordEncoder.encode(newUser.getPassword());
        Patient patient = userMapper.toUser(newUser, encryptedPassword);
        patient.setAuthority(Authority.ROLE_PATIENT);
        patient.setPeselHash(cryptoService.hash(newUser.getPesel()));
        userRepository.save(patient);
    }

    public List<AppointmentsDto> getAppointmentsForCurrentUser() {
        User user = securityService.getCurrentUser();
        Long userId = user.getUserId();
        List<AppointmentsEntity> appointments = appointmentsRepository.findByPatientUserId(userId);
        return appointments.stream()
                .map(appointmentsMapper::toAppointmentDto)
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UsernameNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
