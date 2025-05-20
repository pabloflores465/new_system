package com.example.satsimulationsystem.backend.modules.users.application;

import com.example.satsimulationsystem.backend.modules.users.domain.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user, String creatorRole);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    List<User> getAllUsers();
    User updateUserCredentials(String username, String newPassword, String hospitalCreds, String pharmacyCreds, String insuranceCreds);
    void deleteUser(Long id);
    // More methods will be added for report generation later
} 