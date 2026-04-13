package com.example.medhub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSignupRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String passwordConfirmation;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 48)
    private String name;

    @NotBlank(message = "Surname is required")
    @Size(min = 2, max = 48)
    private String surname;

    @NotBlank(message = "PWZ is required")
    @Pattern(regexp = "^\\d{7}$", message = "PWZ must be 7 digits")
    private String pwz;

    @NotEmpty(message = "At least one specialization is required")
    private List<Long> specializationIds;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
}
