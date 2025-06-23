package com.medical.bookingapi.repository;

import com.medical.bookingapi.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByEmail(String email);
    List<Doctor> findBySpecialty(String specialty);

}