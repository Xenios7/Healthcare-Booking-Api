package com.medical.bookingapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Past;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

/**
 * A GESY beneficiary who books appointments.
 * <p>
 * Shares the same primary key as {@link User} because of
 * {@code @Inheritance(strategy = InheritanceType.JOINED)} on the parent.
 */
@Getter @Setter @NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "patient")
public class Patient extends User {

    @Past
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(columnDefinition = "text")
    private String allergies;

    @Column(name = "insurance_id")
    private String insuranceId;
}
