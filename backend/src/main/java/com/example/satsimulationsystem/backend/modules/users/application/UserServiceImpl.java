package com.example.satsimulationsystem.backend.modules.users.application;

import com.example.satsimulationsystem.backend.modules.users.domain.User;
import com.example.satsimulationsystem.backend.modules.users.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Will be configured in SecurityConfig

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User createUser(User user, String creatorRole) {
        if (!"ADMINISTRATOR".equals(creatorRole)) {
            throw new SecurityException("Only administrators can create users.");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Based on the requirements, only an administrator type user can create other users.
        // The user being created here is for the modules (hospital, farmacia, seguro)
        // So, their isAdminCreator flag should be false.
        user.setAdminCreator(false);
        // The role of the created user will be passed in the User object itself.
        // e.g. "MODULE_HOSPITAL", "MODULE_PHARMACY", "MODULE_INSURANCE"
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateUserCredentials(String username, String newPassword, String hospitalCreds, String pharmacyCreds, String insuranceCreds) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        // Only update credentials if provided
        if (hospitalCreds != null) user.setHospitalServiceCredentials(hospitalCreds);
        if (pharmacyCreds != null) user.setPharmacyServiceCredentials(pharmacyCreds);
        if (insuranceCreds != null) user.setInsuranceServiceCredentials(insuranceCreds);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
} 