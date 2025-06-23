package com.medical.bookingapi.repository;

import com.medical.bookingapi.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    Optional<Patient> findByEmail(String email);
    List<Patient> findByBloodType(String bloodType);
}
