package com.example.satsimulationsystem.backend.modules.users.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "app_users") // "user" is often a reserved keyword in SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Size(max = 120) // Store hashed passwords, so allow more space
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String role; // e.g., "ADMINISTRATOR", "MODULE_HOSPITAL", "MODULE_PHARMACY", "MODULE_INSURANCE"

    // Credentials for module services (if applicable)
    // These would be specific to how you integrate with other modules
    // For now, let's assume these are simple strings or encrypted values
    private String hospitalServiceCredentials;
    private String pharmacyServiceCredentials;
    private String insuranceServiceCredentials;

    // A flag to indicate if the user is an administrator type who can create other users
    private boolean isAdminCreator = false; // Default to false

} 