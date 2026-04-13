package com.example.medhub.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDoctorLocationRequestDto {

    @NotNull
    private Long doctorId;
}
