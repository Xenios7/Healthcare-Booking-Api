package com.medical.bookingapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "admins")
public class Admin extends User {
    // No extra fields for now, but you could add admin-specific data here
}
