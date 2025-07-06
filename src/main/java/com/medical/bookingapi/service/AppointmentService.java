package com.medical.bookingapi.service;

import com.medical.bookingapi.dto.AppointmentCreateDTO;
import com.medical.bookingapi.dto.AppointmentDTO;
import com.medical.bookingapi.model.AppointmentSlot;

import java.util.List;
import java.util.Optional;

public interface AppointmentService {

    List<AppointmentDTO> findByDoctorId(Long doctorId);

    List<AppointmentDTO> findByPatientId(Long patientId);

    Optional<AppointmentDTO> findBySlotId(Long slotId);

    boolean existsBySlot(AppointmentSlot slot);

    List<AppointmentDTO> findByStatus(String status);

    List<AppointmentDTO> findByDoctorAndStatus(Long doctorId, String status);

    AppointmentDTO bookAppointment(AppointmentCreateDTO dto);

    AppointmentDTO updateStatus(Long appointmentId, String newStatus);
}
