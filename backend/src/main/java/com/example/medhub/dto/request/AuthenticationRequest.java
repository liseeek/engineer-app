package com.example.medhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class AuthenticationRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
