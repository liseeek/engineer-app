package com.example.medhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class SpecializationCreateRequestDto {
    @NotBlank
    @Size(min = 3, max = 48)
    @Schema(example = "Stomatolog")
    private String specializationName;
}
