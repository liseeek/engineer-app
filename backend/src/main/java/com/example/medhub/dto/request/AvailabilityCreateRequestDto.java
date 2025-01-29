package com.example.medhub.dto.request;

import com.example.medhub.entity.AppointmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema
public class AvailabilityCreateRequestDto {
    @NotNull
    @Schema(example = "2")
    private Long doctorId;

    @NotNull
    @Schema(example = "2023-01-01")
    private LocalDate date;

    @NotNull
    @Schema(type = "string", format = "time", example = "14:00")
    private LocalTime fromTime;

    @NotNull
    @Schema(type = "string", format = "time", example = "14:00")
    private LocalTime toTime;

    @NotNull
    @Schema(type = "string", example = "30")
    private Long visitTime;

    @NotNull
    private AppointmentType appointmentType;
}
