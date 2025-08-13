package com.medical.bookingapi.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientAdminUpdateDTO {
    private LocalDate dateOfBirth;
    private String bloodType;
    private String allergies;
    private String insuranceId;
}
