package com.medical.bookingapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
