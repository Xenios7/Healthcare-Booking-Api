package com.medical.bookingapi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.medical.bookingapi.dto.PatientDTO;
import com.medical.bookingapi.dto.PatientProfileUpdateDTO;
import com.medical.bookingapi.dto.PatientRegistrationDTO;
import com.medical.bookingapi.mapper.PatientMapper;
import com.medical.bookingapi.model.Patient;
import com.medical.bookingapi.repository.PatientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PatientDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));
        return patientMapper.toDto(patient);
    }

    @Override
    public Optional<PatientDTO> findByEmail(String email) {

        return patientRepository.findByEmail(email)
                .map(patientMapper::toDto);

    }

    @Override
    public List<PatientDTO> findByBloodType(String bloodType) {

        List<Patient> patients = patientRepository.findByBloodType(bloodType);

        return patients.stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PatientDTO createPatient(PatientRegistrationDTO dto) {

        Patient patient = patientMapper.toEntity(dto);
        // Hash the password
        patient.setPassword_hash(passwordEncoder.encode(dto.getPassword()));
        // Set the role
        patient.setRole("PATIENT");

        return patientMapper.toDto(patientRepository.save(patient));
    }

    public PatientDTO me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Patient me = patientRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));
        return patientMapper.toDto(me);
    }    

    public PatientDTO updateMyProfile(PatientProfileUpdateDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Patient me = patientRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

        me.setDateOfBirth(dto.getDateOfBirth());
        me.setBloodType(dto.getBloodType());
        me.setAllergies(dto.getAllergies());
        me.setInsuranceId(dto.getInsuranceId());

        Patient saved = patientRepository.save(me);
        return patientMapper.toDto(saved);
    }

    // @Override
    // public PatientDTO updatePatient(Long id, PatientDTO dto) {

    //     Patient patient = patientRepository.findById(id)
    //             .orElseThrow(() -> new UsernameNotFoundException("Patient not found"));

    //     patient.setAllergies(dto.getAllergies());
    //     patient.setBloodType(dto.getBloodType());
    //     patient.setDateOfBirth(dto.getDateOfBirth());
    //     patient.setInsuranceId(dto.getInsuranceId());

    //     return patientMapper.toDto(patientRepository.save(patient));
    // }

    // @Override
    // public void deletePatient(Long id) {
    //     if (!patientRepository.existsById(id)) {
    //         throw new UsernameNotFoundException("Patient not found");
    //     }
    //     patientRepository.deleteById(id);
    // }

    
}
