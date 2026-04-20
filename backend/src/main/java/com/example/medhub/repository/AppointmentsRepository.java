package com.example.medhub.repository;

import com.example.medhub.entity.AppointmentsEntity;
import com.example.medhub.entity.LocationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppointmentsRepository extends JpaRepository<AppointmentsEntity, Long> {
        List<AppointmentsEntity> findByLocation(LocationEntity location);

        /**
         * Native SQL avoids Hibernate 6 alias bug on soft-deleted {@code User}
         * when filtering by {@code a.patient.userId}.
         */
        @Query(value = "SELECT a.* FROM appointments a "
                        + "INNER JOIN users u ON a.user_id = u.user_id AND u.deleted = false "
                        + "WHERE u.user_id = :patientUserId AND a.deleted = false "
                        + "ORDER BY a.appointment_date ASC, a.appointment_time ASC", nativeQuery = true)
        List<AppointmentsEntity> findAllByPatientUserId(@Param("patientUserId") Long patientUserId);

        /**
         * Native query avoids Hibernate 6 generating invalid SQL for {@code JOIN FETCH a.doctor}
         * with {@link org.hibernate.annotations.SoftDelete} on {@link com.example.medhub.entity.User}
         * (missing alias for users table: {@code d1_1.deleted}).
         */
        @Query(value = "SELECT * FROM appointments WHERE doctor_id = :doctorId AND deleted = false "
                        + "ORDER BY appointment_date ASC, appointment_time ASC", nativeQuery = true)
        List<AppointmentsEntity> findAllByDoctorUserId(@Param("doctorId") Long doctorId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT a FROM AppointmentsEntity a WHERE a.appointmentId = :id")
        Optional<AppointmentsEntity> findWithLockingById(@Param("id") Long id);

        /**
         * Returns scalar columns only (no {@link AppointmentsEntity}) so the service layer never touches
         * {@code appointment.getDoctor()} — lazy-loading {@link com.example.medhub.entity.Doctor} triggers Hibernate 6
         * bug SQL ({@code d1_1.deleted} with missing join) when {@code User} uses {@code @SoftDelete} + JOINED inheritance.
         * <p>
         * Columns: appointment_id, doctor_id, appointment_date, appointment_time, location_id, appointment_status,
         * appointment_type. Only free slots: {@code user_id IS NULL}.
         */
        @Query(value = "SELECT a.appointment_id, a.doctor_id, a.appointment_date, a.appointment_time, "
                        + "a.location_id, a.appointment_status, a.appointment_type "
                        + "FROM appointments a "
                        + "INNER JOIN users doc_u ON a.doctor_id = doc_u.user_id AND doc_u.deleted = false "
                        + "WHERE a.location_id = :locationId AND a.doctor_id = :doctorId "
                        + "AND a.appointment_type = :appointmentType AND a.deleted = false AND a.user_id IS NULL "
                        + "AND a.appointment_status = 'ACTIVE' "
                        + "AND (a.appointment_date > :today "
                        + "     OR (a.appointment_date = :today AND a.appointment_time > :nowTime)) "
                        + "ORDER BY a.appointment_date ASC, a.appointment_time ASC",
                        nativeQuery = true)
        List<Object[]> findAppointmentSlotRowsByFilters(
                        @Param("locationId") Long locationId,
                        @Param("doctorId") Long doctorId,
                        @Param("appointmentType") String appointmentType,
                        @Param("today") LocalDate today,
                        @Param("nowTime") LocalTime nowTime);

        @Query("SELECT DISTINCT a FROM AppointmentsEntity a " +
                        "JOIN FETCH a.doctor d " +
                        "JOIN FETCH a.patient u " +
                        "LEFT JOIN FETCH d.locations " +
                        "WHERE a.location = :location " +
                        "AND a.patient IS NOT NULL")
        List<AppointmentsEntity> findAllScheduledByLocation(@Param("location") LocationEntity location);

        Page<AppointmentsEntity> findAllByLocationAndPatientIsNotNullOrderByDateAscTimeAsc(
                        LocationEntity location, Pageable pageable);

        /**
         * Native SQL avoids Hibernate 6 bug with soft-deleted {@code User} in JPQL path
         * {@code a.patient.userId} ({@code p1_1} alias / missing FROM clause).
         */
        @Query(value = "SELECT COUNT(*) FROM appointments a "
                        + "INNER JOIN users u ON a.user_id = u.user_id AND u.deleted = false "
                        + "WHERE u.user_id = :patientUserId "
                        + "AND a.appointment_status IN (:statuses) AND a.deleted = false "
                        + "AND (a.appointment_date > :minDate "
                        + "     OR (a.appointment_date = :minDate AND a.appointment_time > :nowTime))",
                        nativeQuery = true)
        long countUpcomingForPatient(
                        @Param("patientUserId") Long patientUserId,
                        @Param("minDate") LocalDate minDate,
                        @Param("nowTime") LocalTime nowTime,
                        @Param("statuses") Collection<String> statuses);

        /**
         * Bulk-mark past assigned appointments ({@code patient IS NOT NULL}) with status {@code ACTIVE} or
         * {@code RESCHEDULED} as {@code COMPLETED} once {@code date+time <= now}. Idempotent.
         *
         * @return number of updated rows.
         */
        @Modifying
        @Query(value = "UPDATE appointments "
                        + "SET appointment_status = 'COMPLETED' "
                        + "WHERE deleted = false "
                        + "AND user_id IS NOT NULL "
                        + "AND appointment_status IN ('ACTIVE', 'RESCHEDULED') "
                        + "AND (appointment_date < :today "
                        + "     OR (appointment_date = :today AND appointment_time <= :nowTime))",
                        nativeQuery = true)
        int markPastAppointmentsCompleted(
                        @Param("today") LocalDate today,
                        @Param("nowTime") LocalTime nowTime);
}
