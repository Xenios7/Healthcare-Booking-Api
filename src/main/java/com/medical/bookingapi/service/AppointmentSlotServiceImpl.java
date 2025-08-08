package com.medical.bookingapi.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.medical.bookingapi.dto.AppointmentSlotDTO;
import com.medical.bookingapi.dto.SlotCreateDTO;
import com.medical.bookingapi.mapper.AppointmentSlotMapper;
import com.medical.bookingapi.model.AppointmentSlot;
import com.medical.bookingapi.model.Doctor;
import com.medical.bookingapi.repository.AppointmentSlotRepository;
import com.medical.bookingapi.repository.DoctorRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentSlotServiceImpl implements AppointmentSlotService {

    private final AppointmentSlotRepository appointmentSlotRepository;
    private final AppointmentSlotMapper appointmentSlotMapper;
    private final DoctorRepository doctorRepository; 

    @Override
    public List<AppointmentSlotDTO> findByDoctorId(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with ID: " + doctorId));

        List<AppointmentSlot> slots = appointmentSlotRepository.findByDoctor(doctor);        

        return slots.stream()
                .map(appointmentSlotMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentSlotDTO> findByIsBookedFalse() {
      
        List<AppointmentSlot> slots = appointmentSlotRepository.findByBookedFalse();        

        return slots.stream()
                .map(appointmentSlotMapper::toDto)
                .collect(Collectors.toList());

    }

    @Override
    public Optional<AppointmentSlotDTO> findById(Long id) {
       
        return appointmentSlotRepository
                .findById(id)
                .map(appointmentSlotMapper::toDto);        
    }

    @Override
    public List<AppointmentSlotDTO> findByDoctorAndIsBookedFalse(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with ID: " + doctorId));

        List<AppointmentSlot> slots = appointmentSlotRepository.findByDoctorAndBookedFalse(doctor);        

        return slots.stream()
                .map(appointmentSlotMapper::toDto)
                .collect(Collectors.toList());

    }

    @Override
    public List<AppointmentSlotDTO> findByStartTimeBetween(LocalDateTime start, LocalDateTime end) {
      
        List<AppointmentSlot> slots = appointmentSlotRepository.findByStartTimeBetween(start, end);        

        return slots.stream()
                .map(appointmentSlotMapper::toDto)
                .collect(Collectors.toList());

    }

    @Override
    public Optional<AppointmentSlotDTO> findFirstByDoctorAndIsBookedFalseOrderByStartTimeAsc(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with ID: " + doctorId));

        return appointmentSlotRepository
                .findFirstByDoctorAndBookedFalseOrderByStartTimeAsc(doctor)
                .map(appointmentSlotMapper::toDto);
    }

    @Override
    public AppointmentSlotDTO createSlot(SlotCreateDTO dto) {
        
        AppointmentSlot slot = appointmentSlotMapper.toEntity(dto);

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with ID: " + dto.getDoctorId()));

        //Foreign key, we need to set it manually
        slot.setDoctor(doctor);
        slot.setBooked(false);

        return appointmentSlotMapper.toDto(appointmentSlotRepository.save(slot));

    }
    @Override
    public AppointmentSlotDTO updateSlot(Long id, AppointmentSlotDTO dto) {
        AppointmentSlot slot = appointmentSlotRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Appointment slot not found with ID: " + id));

        boolean wasBooked = slot.isBooked();
        boolean wantsToUnbook = !dto.isBooked();

        if (wasBooked && !wantsToUnbook) {
            throw new IllegalStateException("Cannot update a booked slot.");
        }

        // Set manually since they are foreign keys  
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setBooked(dto.isBooked());
        slot.setNotes(dto.getNotes());

        if (dto.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                    .orElseThrow(() -> new EntityNotFoundException("Doctor not found with ID: " + dto.getDoctorId()));
            slot.setDoctor(doctor);
        }

        return appointmentSlotMapper.toDto(appointmentSlotRepository.save(slot));
    }

    @Override
    public void deleteSlot(Long id) {
        if (!appointmentSlotRepository.existsById(id)) {
            throw new EntityNotFoundException("Appointment slot not found with ID: " + id);
        }
        appointmentSlotRepository.deleteById(id);
    }



}
