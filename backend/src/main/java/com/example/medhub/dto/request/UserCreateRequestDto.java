package com.example.medhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema
public class UserCreateRequestDto {
    @NotBlank
    @Size(min = 3, max = 48)
    @Schema(example = "Jan")
    private String name;

    @NotBlank
    @Size(min = 3, max = 48)
    @Schema(example = "Kowalski")
    private String surname;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    @Size(max = 48)
    @Schema(example = "kowalski99@gmail.com")
    private String email;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")
    @Size(max = 120)
    @Schema(example = "Password1$")
    private String password;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")
    @Size(max = 120)
    @Schema(example = "Password1$")
    private String passwordConfirmation;

    @NotBlank
    @Size(max = 48)
    @Schema(example = "123456789")
    private String phoneNumber;
}
