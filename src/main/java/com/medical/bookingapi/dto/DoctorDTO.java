package com.medical.bookingapi.dto;
import lombok.Data;

@Data
public class DoctorDTO {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    private String specialty;
    private String location;
    private String licenseNumber;
}
