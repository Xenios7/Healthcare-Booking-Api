package com.medical.bookingapi.repository;

import com.medical.bookingapi.model.AppointmentSlot;
import com.medical.bookingapi.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    List<AppointmentSlot> findByDoctor(Doctor doctor);

    List<AppointmentSlot> findByBookedFalse();

    List<AppointmentSlot> findByDoctorAndBookedFalse(Doctor doctor);

    // Find slots within a specific time range
    List<AppointmentSlot> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    // Find the next available slot for a doctor *sos*
    Optional<AppointmentSlot> findFirstByDoctorAndBookedFalseOrderByStartTimeAsc(Doctor doctor);

    // For the SAME doctor, an overlap exists iff: existing.end > new.start AND existing.start < new.end
    // This treats [10:00,10:30] and [10:30,11:00] as NON-overlapping (adjacent is OK).
    @Query("""
        select (count(s) > 0) from AppointmentSlot s
        where s.doctor = :doctor
        and s.endTime   > :start
        and s.startTime < :end
    """)
    boolean existsOverlapping(@Param("doctor") Doctor doctor,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    @Query("""
    select (count(s) > 0) from AppointmentSlot s
    where s.doctor = :doctor
        and s.id <> :id
        and s.endTime   > :start
        and s.startTime < :end
    """)
    boolean existsOverlappingExcludingId(@Param("doctor") Doctor doctor,
                                        @Param("id") Long id,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

}
