package com.example.medhub.mapper;

import com.example.medhub.dto.response.InvitationDetailsDto;
import com.example.medhub.entity.Invitation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvitationMapper {

    @Mapping(target = "locationName", expression = "java(invitation.getLocation() != null ? invitation.getLocation().getLocationName() : null)")
    @Mapping(target = "specializationName", expression = "java(invitation.getSpecialization() != null ? invitation.getSpecialization().getSpecializationName() : null)")
    InvitationDetailsDto toInvitationDetails(Invitation invitation);
}
