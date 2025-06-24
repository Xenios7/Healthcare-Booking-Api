package com.medical.bookingapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false) //Many appointments → one patient
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(optional = false) //Many appointments for one doctor
    @JoinColumn(name = "doctor_id", nullable = false)   
    private Doctor doctor;

    @NotNull
    @OneToOne(optional = false) //One appointment → one slot    
    @JoinColumn(name = "slot_id", unique = true, nullable = false)
    private AppointmentSlot slot;

    @Column(nullable = false)
    private String status = "BOOKED";

    @Column(columnDefinition = "text") 
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
