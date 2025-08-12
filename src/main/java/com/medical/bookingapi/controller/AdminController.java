package com.medical.bookingapi.controller;

import com.medical.bookingapi.auth.AuthService;
import com.medical.bookingapi.dto.DoctorRegistrationDTO;
import com.medical.bookingapi.dto.RegisterRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;

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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ping")
    public String adminPing() {
        return "admin ok";
    }
}