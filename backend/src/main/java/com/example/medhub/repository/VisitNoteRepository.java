package com.example.medhub.repository;

import com.example.medhub.entity.VisitNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisitNoteRepository extends JpaRepository<VisitNote, Long> {
    Optional<VisitNote> findByAppointment_AppointmentId(Long appointmentId);
}
