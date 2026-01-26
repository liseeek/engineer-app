package com.example.medhub.service;

import com.example.medhub.dto.VisitNoteRequestDto;
import com.example.medhub.dto.AppointmentsDto;
import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.User;
import com.example.medhub.entity.VisitNote;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.exceptions.UnauthorizedOperationException;
import com.example.medhub.mapper.AppointmentsMapper;
import com.example.medhub.repository.AppointmentsRepository;
import com.example.medhub.repository.VisitNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import com.example.medhub.aspect.annotation.Auditable;

@Service
@RequiredArgsConstructor
public class DoctorWorkspaceService {

    private final AppointmentsRepository appointmentsRepository;
    private final VisitNoteRepository visitNoteRepository;
    private final AppointmentsMapper appointmentsMapper;
    private final SecurityService securityService;

    public List<AppointmentsDto> getMySchedule() {
        User currentUser = securityService.getCurrentUser();

        return appointmentsRepository.findAllByDoctorUserId(currentUser.getUserId())
                .stream()
                .map(appointmentsMapper::toAppointmentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Auditable(action = "CREATE_MEDICAL_RECORD", resourceId = "#appointmentId")
    public void concludeVisit(Long appointmentId, VisitNoteRequestDto noteDto) {
        User currentUser = securityService.getCurrentUser();

        AppointmentsEntity appointment = appointmentsRepository.findById(appointmentId)
                .orElseThrow(() -> new MedHubServiceException("Appointment not found"));

        if (!appointment.getDoctor().getUserId().equals(currentUser.getUserId())) {
            throw new UnauthorizedOperationException(
                    "Unauthorized: This appointment does not belong to you.");
        }

        VisitNote note = VisitNote.builder()
                .appointment(appointment)
                .doctor(appointment.getDoctor())
                .diagnosis(noteDto.getDiagnosis())
                .prescription(noteDto.getPrescription())
                .notes(noteDto.getNotes())
                .build();

        visitNoteRepository.save(note);

        appointment.setAppointmentStatus(AppointmentStatus.COMPLETED);
        appointmentsRepository.save(appointment);
    }
}
