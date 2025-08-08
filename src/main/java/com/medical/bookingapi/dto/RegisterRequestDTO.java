package com.medical.bookingapi.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role; // "DOCTOR", "PATIENT", "ADMIN"
    
    // Only required for doctors
    private String location;
    private String speciality;
}
