package com.medical.bookingapi.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.medical.bookingapi.dto.AppointmentCreateDTO;
import com.medical.bookingapi.dto.AppointmentDTO;
import com.medical.bookingapi.mapper.AppointmentMapper;
import com.medical.bookingapi.model.Appointment;
import com.medical.bookingapi.model.AppointmentSlot;
import com.medical.bookingapi.model.Doctor;
import com.medical.bookingapi.model.Patient;
import com.medical.bookingapi.repository.AppointmentRepository;
import com.medical.bookingapi.repository.DoctorRepository;
import com.medical.bookingapi.repository.PatientRepository;

import com.medical.bookingapi.repository.AppointmentSlotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentSlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    public List<AppointmentDTO> findByDoctorId(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));
        
        List<Appointment> appointments = appointmentRepository.findByDoctor(doctor);

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<AppointmentDTO> findByPatientId(Long patientId) {
    
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

        List<Appointment> appointments = appointmentRepository.findByPatient(patient);

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());        
    }
    
    @Override
    public Optional<AppointmentDTO> findBySlotId(Long slotId) {
        AppointmentSlot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new EntityNotFoundException("Slot not found"));

        return appointmentRepository.findBySlot(slot)
                .map(appointmentMapper::toDto);
    }

    @Override
    public List<AppointmentDTO> findByStatus(String status) {

        List<Appointment> appointments = appointmentRepository.findByStatus(status);

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsBySlot(AppointmentSlot slot) {

        return appointmentRepository.existsBySlot(slot);

    }

    @Override
    public List<AppointmentDTO> findByDoctorAndStatus(Long doctorId, String status) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));
        List<Appointment> appointments = appointmentRepository.findByDoctorAndStatus(doctor, status);

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());        
    }

@Override
public AppointmentDTO bookAppointment(AppointmentCreateDTO dto) {
    // Resolve FKs
    Doctor doctor = doctorRepository.findById(dto.getDoctorId())
        .orElseThrow(() -> new EntityNotFoundException("Doctor not found"));

    Patient patient = patientRepository.findById(dto.getPatientId())
        .orElseThrow(() -> new EntityNotFoundException("Patient not found"));

    AppointmentSlot slot = slotRepository.findById(dto.getSlotId())
        .orElseThrow(() -> new EntityNotFoundException("Slot not found"));

    // Guard: slot must belong to the same doctor the patient picked
    if (slot.getDoctor() == null || !slot.getDoctor().getId().equals(doctor.getId())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot does not belong to the selected doctor");
    }

    // Guard: slot must be free
    if (slot.isBooked() || appointmentRepository.existsBySlot(slot)) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot already booked");
    }

    // Map DTO -> entity (notes, etc.)
    Appointment appointment = appointmentMapper.toEntity(dto);
    appointment.setDoctor(doctor);
    appointment.setPatient(patient);
    appointment.setSlot(slot);

    // Align with UI workflow: new appointments start as PENDING
    appointment.setStatus("PENDING");

    Appointment saved = appointmentRepository.save(appointment);

    // Reserve the slot so it disappears from "available" lists
    slot.setBooked(true);
    slotRepository.save(slot);

    return appointmentMapper.toDto(saved);
}

@Override
public AppointmentDTO updateStatus(Long id, String newStatus) {
    Appointment appointment = appointmentRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Appointment not found with ID: " + id));

    String current = appointment.getStatus();
    newStatus = newStatus.toUpperCase();

    // Allowed transitions for your UI:
    // PENDING -> APPROVED | REJECTED | CANCELLED
    // APPROVED -> CANCELLED
    // REJECTED -> (none)
    // CANCELLED -> (none)
    boolean ok =
        ("PENDING".equals(current) && Set.of("APPROVED", "REJECTED", "CANCELLED").contains(newStatus)) ||
        ("APPROVED".equals(current) && "CANCELLED".equals(newStatus));

    if (!ok) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Invalid status transition: " + current + " -> " + newStatus);
    }

    appointment.setStatus(newStatus);

    // Free the slot when the appt is REJECTED or CANCELLED
    if ("REJECTED".equals(newStatus) || "CANCELLED".equals(newStatus)) {
        AppointmentSlot slot = appointment.getSlot();
        if (slot != null && slot.isBooked()) {
            slot.setBooked(false);
            slotRepository.save(slot);
        }
    }

    return appointmentMapper.toDto(appointmentRepository.save(appointment));
}
    @Override
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Appointment not found with ID: " + id);
        }
        appointmentRepository.deleteById(id);
    }

}
