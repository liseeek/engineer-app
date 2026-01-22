package com.example.medhub.dto.request;

import com.example.medhub.validation.ValidPwz;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema
public class DoctorCreateRequestDto {

    @NotBlank
    @Size(min = 3, max = 48)
    @Schema(example = "John")
    private String name;

    @NotBlank
    @Size(min = 3, max = 48)
    @Schema(example = "Doe")
    private String surname;

    @ValidPwz
    @NotBlank
    @Size(max = 7)
    @Schema(example = "1234567")
    private String pwz;

    @NotBlank
    @Size(max = 255)
    @Schema(example = "Central Hospital")
    private String locationName;

    @Schema(example = "1")
    private Long specializationId;
}
