package com.medical.bookingapi.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.medical.bookingapi.service.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    
    private final AppointmentService appointmentService;

    @GetMapping("/by-doctor/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctorId(@PathVariable Long doctorId){
        List<AppointmentDTO> appointments = appointmentService.findByDoctorId(doctorId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/by-patient/{patientId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentByPatientId(@PathVariable Long patientId){
        List<AppointmentDTO> appointments = appointmentService.findByPatientId(patientId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/by-slot/{slotId}")
    public ResponseEntity<AppointmentDTO> getAppointmentBySlotId(@PathVariable Long slotId){
        return appointmentService.findBySlotId(slotId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentByStatus(@PathVariable String status){

        List<AppointmentDTO> appointments = appointmentService.findByStatus(status);
        return ResponseEntity.ok(appointments);

    }

    @GetMapping("/by-doctor/{doctorId}/status/{status}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentByDoctorAndStatus(
        @PathVariable Long doctorId,
        @PathVariable String status){

            List<AppointmentDTO> appointments = appointmentService.findByDoctorAndStatus(doctorId, status);
            return ResponseEntity.ok(appointments);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<AppointmentDTO> bookAppointment(@RequestBody @Valid AppointmentCreateDTO dto){

        AppointmentDTO appointment = appointmentService.bookAppointment(dto);
        return ResponseEntity.ok(appointment);

    }


    // Only DOCTOR can update status 
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        AppointmentDTO updated = appointmentService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build(); 
    }    

}

