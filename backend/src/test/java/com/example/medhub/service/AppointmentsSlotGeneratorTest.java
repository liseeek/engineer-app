package com.example.medhub.service;

import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.DoctorEntity;
import com.example.medhub.entity.LocationEntity;
import com.example.medhub.enums.AppointmentType;
import com.example.medhub.exceptions.MedHubServiceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentsSlotGeneratorTest {

    private AppointmentsSlotGenerator generator;
    private DoctorEntity doctor;
    private LocationEntity location;

    @BeforeEach
    void setUp() {
        generator = new AppointmentsSlotGenerator();
        doctor = new DoctorEntity();
        location = new LocationEntity();
    }

    @Test
    void generateSlots_ShouldReturnTwoSlots_WhenRangeIsOneHourAndVisitIs30Min() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime fromTime = LocalTime.of(8, 0);
        LocalTime toTime = LocalTime.of(9, 0);
        Long visitTime = 30L;

        List<AppointmentsEntity> slots = generator.generateSlots(
                doctor, location, date, fromTime, toTime, visitTime, AppointmentType.NFZ);

        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).getTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(slots.get(1).getTime()).isEqualTo(LocalTime.of(8, 30));
    }

    @Test
    void generateSlots_ShouldReturnEmptyList_WhenFromTimeEqualsToTime() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime fromTime = LocalTime.of(8, 0);
        LocalTime toTime = LocalTime.of(8, 0);
        Long visitTime = 30L;

        List<AppointmentsEntity> slots = generator.generateSlots(
                doctor, location, date, fromTime, toTime, visitTime, AppointmentType.NFZ);

        assertThat(slots).isEmpty();
    }

    @Test
    void generateSlots_ShouldReturnEmptyList_WhenVisitTimeExceedsRange() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime fromTime = LocalTime.of(8, 0);
        LocalTime toTime = LocalTime.of(8, 30);
        Long visitTime = 60L;

        List<AppointmentsEntity> slots = generator.generateSlots(
                doctor, location, date, fromTime, toTime, visitTime, AppointmentType.NFZ);

        assertThat(slots).isEmpty();
    }

    @Test
    void generateSlots_ShouldThrowException_WhenVisitTimeIsZeroOrNegative() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime fromTime = LocalTime.of(8, 0);
        LocalTime toTime = LocalTime.of(9, 0);

        assertThatThrownBy(() -> generator.generateSlots(
                doctor, location, date, fromTime, toTime, 0L, AppointmentType.NFZ))
                .isInstanceOf(MedHubServiceException.class)
                .hasMessageContaining("greater than 0");
    }
}