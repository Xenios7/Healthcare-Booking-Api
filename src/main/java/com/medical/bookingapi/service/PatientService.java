package com.medical.bookingapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.method.P;

import com.medical.bookingapi.dto.PatientDTO;
import com.medical.bookingapi.dto.PatientRegistrationDTO;
import com.medical.bookingapi.model.Patient;

public interface PatientService {

    Optional<PatientDTO> findByEmail(String email);
    List<PatientDTO> findByBloodType(String bloodType);

    PatientDTO createPatient(PatientRegistrationDTO dto);
    PatientDTO updatePatient(Long id, PatientDTO dto);

    void deletePatient(Long id);
    
}
