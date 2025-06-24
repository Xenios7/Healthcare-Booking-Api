package com.medical.bookingapi.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    private LocalDate dateOfBirth;
    private String bloodType;
    private String allergies;
    private String insuranceId;
}
