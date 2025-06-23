package com.medical.bookingapi.repository;

import com.medical.bookingapi.model.Appointment;
import com.medical.bookingapi.model.Doctor;
import com.medical.bookingapi.model.Patient;
import com.medical.bookingapi.model.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctor(Doctor doctor);

    List<Appointment> findByPatient(Patient patient);

    Optional<Appointment> findBySlot(AppointmentSlot slot);

    boolean existsBySlot(AppointmentSlot slot);

    List<Appointment> findByStatus(String status);

    // Get all appointments for a doctor with a specific status (CANCELLED, BOOKED etc.)
    List<Appointment> findByDoctorAndStatus(Doctor doctor, String status);
}
