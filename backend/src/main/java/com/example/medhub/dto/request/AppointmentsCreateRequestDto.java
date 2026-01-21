package com.example.medhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema
public class AppointmentsCreateRequestDto {

    @Schema(example = "1")
    private Long userId;

    @NotNull
    @Schema(example = "2")
    private Long doctorId;

    @NotNull
    @Schema(example = "2023-01-01")
    private LocalDate date;

    @NotNull
    @Schema(type = "string", format = "time", example = "14:00")
    private LocalTime time;

    @NotNull
    private Long locationId;

}
