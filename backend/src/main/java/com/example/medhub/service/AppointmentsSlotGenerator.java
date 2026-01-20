package com.example.medhub.service;

import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.DoctorEntity;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.enums.AppointmentStatus;
import com.example.medhub.enums.AppointmentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AppointmentsSlotGenerator {

    public List<AppointmentsEntity> generateSlots(
            DoctorEntity doctor,
            LocationEntity location,
            LocalDate date,
            LocalTime fromTime,
            LocalTime toTime,
            Long visitTime,
            AppointmentType appointmentType
    ) {
        List<AppointmentsEntity> slots = new ArrayList<>();

        for (LocalTime time = fromTime; time.isBefore(toTime);
             time = time.plusMinutes(visitTime)) {
            AppointmentsEntity appointment = AppointmentsEntity.builder()
                    .doctor(doctor)
                    .date(date)
                    .time(time)
                    .location(location)
                    .appointmentStatus(AppointmentStatus.ACTIVE)
                    .appointmentType(appointmentType)
                    .build();
            slots.add(appointment);
        }
        return slots;
    }
}
