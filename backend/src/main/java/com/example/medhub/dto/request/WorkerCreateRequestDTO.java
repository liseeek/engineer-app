package com.example.medhub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema
public class WorkerCreateRequestDTO extends UserCreateRequestDto {
    @NotBlank
    @Size(max = 48)
    @Schema(example = "Max-Med")
    private String locationName;
}
