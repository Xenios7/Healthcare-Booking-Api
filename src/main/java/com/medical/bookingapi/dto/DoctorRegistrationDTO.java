package com.medical.bookingapi.dto;

import lombok.Data;

@Data
public class DoctorRegistrationDTO {
    
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    private String speciality;
    private String location;
    private String licenseNumber;
}
