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
@Getter @Setter @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)           // keeps id-based equality from User
                                               //includes fields from the superclass (User) when generating equals() and hashCode().
@Entity
@Table(name = "patient")

public class Patient extends User {

    @Past
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(columnDefinition = "text") //Could be blank
    private String allergies;

    /** Social-insurance or GESY beneficiary number. */
    @Column(name = "insurance_id")
    private String insuranceId;

}
