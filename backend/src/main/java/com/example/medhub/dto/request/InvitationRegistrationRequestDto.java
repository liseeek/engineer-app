package com.example.medhub.dto.request;

import com.example.medhub.validation.ValidPassword;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationRegistrationRequestDto {

    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Surname is required")
    private String surname;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotBlank(message = "Password confirmation is required")
    @ValidPassword
    private String passwordConfirmation;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    /**
     * Required when registering via a DOCTOR invitation (validated in {@link com.example.medhub.service.InvitationRegistrationService}).
     * Ignored for other roles.
     */
    @Pattern(regexp = "^$|^\\d{7}$", message = "PWZ must be exactly 7 digits")
    private String pwz;
}
