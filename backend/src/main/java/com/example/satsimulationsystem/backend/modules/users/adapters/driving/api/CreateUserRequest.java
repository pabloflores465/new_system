package com.example.satsimulationsystem.backend.modules.users.adapters.driving.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 120) // Min password length for security
    private String password;

    @NotBlank
    @Size(max = 50)
    private String role; // e.g., "MODULE_HOSPITAL", "MODULE_PHARMACY", "MODULE_INSURANCE"

    // Credentials for other modules - these will be set up by the admin
    // They are specific to the user being created for a module
    private String hospitalServiceCredentials;
    private String pharmacyServiceCredentials;
    private String insuranceServiceCredentials;
} 