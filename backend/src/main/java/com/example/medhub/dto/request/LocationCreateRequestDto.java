package com.example.medhub.dto.request;

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
public class LocationCreateRequestDto {
    @NotBlank
    @Size(min = 3, max = 48)
    private String locationName;

    @NotBlank
    @Size(min = 3, max = 48)
    private String address;

    @NotBlank
    @Size(min = 3, max = 48)
    private String city;

    @NotBlank
    @Size(min = 3, max = 48)
    private String country;
}
