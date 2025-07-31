package com.medical.bookingapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true) // Includes `id` from User in equals/hashCode
@Entity
@Table(name = "doctor")
public class Doctor extends User{
    
    @NotBlank
    @Column(nullable = false)
    private String speciality;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @Column(name = "license_number")
    private String licenseNumber;

}   
