package com.medical.bookingapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

// DoctorAdminUpdateDTO.java
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorAdminUpdateDTO {
    private String speciality;   // matches entity
    private String location;
    private String licenseNumber;
}
