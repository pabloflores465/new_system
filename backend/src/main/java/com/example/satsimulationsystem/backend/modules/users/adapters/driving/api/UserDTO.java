package com.example.satsimulationsystem.backend.modules.users.adapters.driving.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String role;
    private boolean isAdminCreator;

    // Re-adding credential fields for pre-filling edit form
    private String hospitalServiceCredentials;
    private String pharmacyServiceCredentials;
    private String insuranceServiceCredentials;

    public UserDTO(Long id, String username, String role, boolean isAdminCreator,
                     String hospitalServiceCredentials, String pharmacyServiceCredentials, String insuranceServiceCredentials) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.isAdminCreator = isAdminCreator;
        this.hospitalServiceCredentials = hospitalServiceCredentials;
        this.pharmacyServiceCredentials = pharmacyServiceCredentials;
        this.insuranceServiceCredentials = insuranceServiceCredentials;
    }

    // Constructor for when credentials are not needed (e.g. if we had different DTOs)
    // For now, the main constructor includes them.
    // public UserDTO(Long id, String username, String role, boolean isAdminCreator) {
    //    this(id, username, role, isAdminCreator, null, null, null);
    // }
} 