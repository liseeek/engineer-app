package com.example.medhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDetailsDto {
    private String email;
    private String role;
    private String locationName;
    private String specializationName;
    private String pwz;
}
