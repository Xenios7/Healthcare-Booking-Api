package com.medical.bookingapi.service;

import com.medical.bookingapi.dto.AppointmentCreateDTO;
import com.medical.bookingapi.dto.AppointmentDTO;
import com.medical.bookingapi.model.AppointmentSlot;
import com.medical.bookingapi.model.Doctor;
import com.medical.bookingapi.model.Patient;

import java.util.List;
import java.util.Optional;

public interface AppointmentService {

    List<AppointmentDTO> findByDoctor(Doctor doctor);

    List<AppointmentDTO> findByPatient(Patient patient);

    Optional<AppointmentDTO> findBySlot(AppointmentSlot slot);

    boolean existsBySlot(AppointmentSlot slot);

    List<AppointmentDTO> findByStatus(String status);

    List<AppointmentDTO> findByDoctorAndStatus(Doctor doctor, String status);

    AppointmentDTO bookAppointment(AppointmentCreateDTO dto);

    AppointmentDTO updateStatus(Long appointmentId, String newStatus);
}
