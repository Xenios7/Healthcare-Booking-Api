package com.medical.bookingapi.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medical.bookingapi.dto.AppointmentCreateDTO;
import com.medical.bookingapi.dto.AppointmentDTO;
import com.medical.bookingapi.dto.AppointmentSlotDTO;
import com.medical.bookingapi.dto.SlotCreateDTO;
import com.medical.bookingapi.model.AppointmentSlot;
import com.medical.bookingapi.service.AppointmentService;
import com.medical.bookingapi.service.AppointmentSlotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appointmentSlots")
@RequiredArgsConstructor
public class AppointmentSlotController {
 
    private final AppointmentSlotService appointmentSlotService;
    
    @GetMapping("/by-doctor/{doctorId}")
    public ResponseEntity<List<AppointmentSlotDTO>> getSlotByDoctorId(@PathVariable Long doctorId){
        List<AppointmentSlotDTO> slots = appointmentSlotService.findByDoctorId(doctorId);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/available")
    public ResponseEntity<List<AppointmentSlotDTO>> getAvailableSlots(){
        List<AppointmentSlotDTO> slots = appointmentSlotService.findByIsBookedFalse();
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentSlotDTO> getSlotById(@PathVariable Long id){

        return appointmentSlotService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-doctor/{doctorId}/available")
    public ResponseEntity<List<AppointmentSlotDTO>> getDoctorAvailableSlots(@PathVariable Long doctorId){
        List<AppointmentSlotDTO> slots = appointmentSlotService.findByDoctorAndIsBookedFalse(doctorId);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/between")
    public ResponseEntity<List<AppointmentSlotDTO>> getSlotsBetween(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<AppointmentSlotDTO> slots = appointmentSlotService.findByStartTimeBetween(start, end);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/by-doctor/{doctorId}/available/sorted")    
    public ResponseEntity<AppointmentSlotDTO> getDoctorAvailableSlotsByStartTimeAsc(@PathVariable Long doctorId){

        return appointmentSlotService.findFirstByDoctorAndIsBookedFalseOrderByStartTimeAsc(doctorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
  
    @PostMapping
    public ResponseEntity<AppointmentSlotDTO> createSlot(@RequestBody @Valid SlotCreateDTO dto){
        AppointmentSlotDTO slot = appointmentSlotService.createSlot(dto);
        return ResponseEntity.ok(slot);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentSlotDTO> updateSlot(@PathVariable Long id, @RequestBody @Valid AppointmentSlotDTO dto){
        AppointmentSlotDTO slot = appointmentSlotService.updateSlot(id, dto);
        return ResponseEntity.ok(slot);
    }

    @DeleteMapping
    public ResponseEntity<

}
