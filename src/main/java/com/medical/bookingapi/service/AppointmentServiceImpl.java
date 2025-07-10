package com.medical.bookingapi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

import jakarta.persistence.EntityNotFoundException;

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
            .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));
        
        List<Appointment> appointments = appointmentRepository.findByDoctor(doctor);

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<AppointmentDTO> findByPatientId(Long patientId) {
    
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        List<Appointment> appointments = appointmentRepository.findByPatient(patient);

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());        
    }
    
    @Override
    public Optional<AppointmentDTO> findBySlotId(Long slotId) {
        AppointmentSlot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new UsernameNotFoundException("Slot not found"));

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
            .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));
        List<Appointment> appointments = appointmentRepository.findByDoctorAndStatus(doctor, status);

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());        
    }

    @Override
    public AppointmentDTO bookAppointment(AppointmentCreateDTO dto) {

        Appointment appointment = appointmentMapper.toEntity(dto);

        //Need to manually set this fields since they are foreign keys
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        AppointmentSlot slot = slotRepository.findById(dto.getSlotId())
                .orElseThrow(() -> new UsernameNotFoundException("Slot not found"));

        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setSlot(slot);
        appointment.setStatus("BOOKED");
         
        return appointmentMapper.toDto(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentDTO updateStatus(Long appointmentId, String newStatus) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new UsernameNotFoundException("Appointment not found"));
        
        appointment.setStatus(newStatus);
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
