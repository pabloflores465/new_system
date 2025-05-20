package com.example.satsimulationsystem.backend.modules.users.adapters.driving.api;

import com.example.satsimulationsystem.backend.modules.users.application.UserService;
import com.example.satsimulationsystem.backend.modules.users.domain.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    private UserDTO convertToDto(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isAdminCreator(),
                user.getHospitalServiceCredentials(),
                user.getPharmacyServiceCredentials(),
                user.getInsuranceServiceCredentials()
        );
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest createUserRequest, Authentication authentication) {
        // Assuming the "ADMINISTRATOR" role is represented as "ROLE_ADMIN" in Spring Security
        String creatorRole = authentication.getAuthorities().stream()
                                   .map(GrantedAuthority::getAuthority)
                                   // Spring Security authorities are often prefixed with "ROLE_"
                                   // The service layer expects the role name without the prefix, e.g., "ADMINISTRATOR"
                                   .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                                   .findFirst()
                                   .orElseThrow(() -> new IllegalStateException("User creating role could not be determined"));

        User newUser = new User();
        newUser.setUsername(createUserRequest.getUsername());
        newUser.setPassword(createUserRequest.getPassword()); // Password will be encoded by the service
        newUser.setRole(createUserRequest.getRole());
        newUser.setHospitalServiceCredentials(createUserRequest.getHospitalServiceCredentials());
        newUser.setPharmacyServiceCredentials(createUserRequest.getPharmacyServiceCredentials());
        newUser.setInsuranceServiceCredentials(createUserRequest.getInsuranceServiceCredentials());

        User createdUser = userService.createUser(newUser, creatorRole);
        return new ResponseEntity<>(convertToDto(createdUser), HttpStatus.CREATED);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(convertToDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{username}/credentials")
    public ResponseEntity<UserDTO> updateUserCredentials(@PathVariable String username, @Valid @RequestBody UpdateCredentialsRequest request) {
        try {
            User updatedUser = userService.updateUserCredentials(username, request.getNewPassword(),
                    request.getHospitalServiceCredentials(), request.getPharmacyServiceCredentials(), request.getInsuranceServiceCredentials());
            return ResponseEntity.ok(convertToDto(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 