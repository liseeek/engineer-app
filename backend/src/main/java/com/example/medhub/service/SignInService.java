package com.example.medhub.service;


import com.example.medhub.configuration.security.jwt.JwtService;
import com.example.medhub.dto.request.AuthenticationRequest;
import com.example.medhub.dto.request.AuthenticationResponse;
import com.example.medhub.entity.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SignInService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse signIn(AuthenticationRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
                        request.getPassword()));
        User user = (User) authentication.getPrincipal();
        var jwt = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .jwtToken(jwt)
                .authority(user.getFirstAuthority())
                .build();
    }
}
