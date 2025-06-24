package com.medical.bookingapi.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRegistrationDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;         // Will be hashed using BCrypt in the service layer

    private LocalDate dateOfBirth;
    private String bloodType;
    private String allergies;
    private String insuranceId;
}
