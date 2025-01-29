package com.example.medhub.dto.request;

import com.example.medhub.entity.Authority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String jwtToken;
    private Authority authority;
}
