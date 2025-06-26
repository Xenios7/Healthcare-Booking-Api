package com.medical.bookingapi.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.medical.bookingapi.dto.AppointmentSlotDTO;
import com.medical.bookingapi.dto.SlotCreateDTO;
import com.medical.bookingapi.model.Doctor;

public interface AppointmentSlotService {
    

    List<AppointmentSlotDTO> findByDoctor(Doctor doctor);

    List<AppointmentSlotDTO> findByIsBookedFalse();

    List<AppointmentSlotDTO> findByDoctorAndIsBookedFalse(Doctor doctor);

    List<AppointmentSlotDTO> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    // Find the next available slot for a doctor
    Optional<AppointmentSlotDTO> findFirstByDoctorAndIsBookedFalseOrderByStartTimeAsc(Doctor doctor);
    //Added..
    AppointmentSlotDTO createSlot(SlotCreateDTO dto);
    
    AppointmentSlotDTO updateSlot(Long id, AppointmentSlotDTO dto);

    void deleteSlot(Long id);

    Optional<AppointmentSlotDTO> findById(Long id);

}
