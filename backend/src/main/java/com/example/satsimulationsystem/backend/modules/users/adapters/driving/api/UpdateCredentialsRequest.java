package com.example.satsimulationsystem.backend.modules.users.adapters.driving.api;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCredentialsRequest {
    @Size(min = 6, max = 120)
    private String newPassword; // Optional: only if password needs to be changed

    private String hospitalServiceCredentials;
    private String pharmacyServiceCredentials;
    private String insuranceServiceCredentials;
} 