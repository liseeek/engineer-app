package com.example.medhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema
public class UpdateDoctorLocationRequestDto {
    Long locationId;
    OperationType operationType;
}
