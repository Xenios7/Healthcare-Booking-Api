// com.medical.bookingapi.dto.PatientProfileUpdateDTO
package com.medical.bookingapi.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PatientProfileUpdateDTO {
    private LocalDate dateOfBirth;
    private String bloodType;
    private String allergies;
    private String insuranceId;
}
