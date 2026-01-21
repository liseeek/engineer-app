package com.example.medhub.dto.request;

import com.example.medhub.enums.AppointmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
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
public class AvailabilityCreateRequestDto {
    @NotNull
    @Schema(example = "2")
    private Long doctorId;

    @NotNull
    @FutureOrPresent
    @Schema(example = "2023-01-01")
    private LocalDate date;

    @NotNull
    @Schema(type = "string", format = "time", example = "14:00")
    private LocalTime fromTime;

    @NotNull
    @Schema(type = "string", format = "time", example = "14:00")
    private LocalTime toTime;

    @NotNull
    @Min(1)
    @Schema(type = "string", example = "30")
    private Long visitTime;

    @NotNull
    private AppointmentType appointmentType;
}
