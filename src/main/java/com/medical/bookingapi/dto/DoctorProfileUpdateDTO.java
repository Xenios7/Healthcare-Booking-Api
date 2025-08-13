package com.medical.bookingapi.dto;

import lombok.Data;

// what a DOCTOR can change about themselves
@Data
public class DoctorProfileUpdateDTO {
    private String firstName;
    private String lastName;
    private String speciality;
    private String location;
    private String licenseNumber; 
}
