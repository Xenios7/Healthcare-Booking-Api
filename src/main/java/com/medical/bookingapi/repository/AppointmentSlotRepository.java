package com.medical.bookingapi.repository;

import com.medical.bookingapi.model.AppointmentSlot;
import com.medical.bookingapi.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
