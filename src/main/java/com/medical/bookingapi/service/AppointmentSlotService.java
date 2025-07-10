package com.medical.bookingapi.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.medical.bookingapi.dto.AppointmentSlotDTO;
import com.medical.bookingapi.dto.SlotCreateDTO;
import com.medical.bookingapi.model.Doctor;

public interface AppointmentSlotService {
    

    List<AppointmentSlotDTO> findByDoctorId(Long doctorId);

    List<AppointmentSlotDTO> findByIsBookedFalse();

    List<AppointmentSlotDTO> findByDoctorAndIsBookedFalse(Long doctorId);

    List<AppointmentSlotDTO> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    Optional<AppointmentSlotDTO> findFirstByDoctorAndIsBookedFalseOrderByStartTimeAsc(Long doctorId);
    
    AppointmentSlotDTO createSlot(SlotCreateDTO dto);
    
    AppointmentSlotDTO updateSlot(Long id, AppointmentSlotDTO dto);

    void deleteSlot(Long id);

    Optional<AppointmentSlotDTO> findById(Long id);

}
