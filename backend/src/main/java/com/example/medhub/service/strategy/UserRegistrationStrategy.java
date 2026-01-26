package com.example.medhub.service.strategy;

import com.example.medhub.dto.request.InvitationRegistrationRequestDto;
import com.example.medhub.entity.Invitation;
import com.example.medhub.entity.User;

public interface UserRegistrationStrategy {

    boolean supports(String role);

    User register(InvitationRegistrationRequestDto request, Invitation invitation);
}
