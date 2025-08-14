package com.medical.bookingapi.controller;

import com.medical.bookingapi.auth.AuthService;
import com.medical.bookingapi.dto.DoctorAdminUpdateDTO;
import com.medical.bookingapi.dto.DoctorDTO;
import com.medical.bookingapi.dto.DoctorRegistrationDTO;
import com.medical.bookingapi.dto.PatientAdminUpdateDTO;
import com.medical.bookingapi.dto.PatientDTO;
import com.medical.bookingapi.dto.RegisterRequestDTO;
import com.medical.bookingapi.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;
    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/doctors")
    public ResponseEntity<String> createDoctor(@RequestBody @Valid DoctorRegistrationDTO dto) {
        authService.registerDoctor(dto);
        return ResponseEntity.status(201).body("Doctor created successfully.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admins")
    public ResponseEntity<String> createAdmin(@RequestBody @Valid RegisterRequestDTO dto) {
        authService.registerAdmin(dto);
        return ResponseEntity.status(201).body("Admin created successfully.");
    }

    // Patients
    @PutMapping("/patients/{id}") 
    public PatientDTO updatePatient(@PathVariable Long id, @RequestBody @Valid PatientAdminUpdateDTO dto){
         return adminService.updatePatient(id,dto); 
    }
    @DeleteMapping("/patients/{id}") 
    public void deletePatient(@PathVariable Long id){ 
        adminService.deletePatient(id); 
    }

    // Doctors
    @PutMapping("/doctors/{id}") 
    public DoctorDTO updateDoctor(@PathVariable Long id, @RequestBody @Valid DoctorAdminUpdateDTO dto){ 
        return adminService.updateDoctor(id,dto); 
    }
    @DeleteMapping("/doctors/{id}") 
    public void deleteDoctor(@PathVariable Long id){ 
        adminService.deleteDoctor(id); 
    }
    

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ping")
    public String adminPing() {
        return "admin ok";
    }
}
