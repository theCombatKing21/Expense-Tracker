package com.SpringBootProject.ExpenseTracker.service;

import com.SpringBootProject.ExpenseTracker.dto.UserRequest;
import com.SpringBootProject.ExpenseTracker.dto.UserResponse;
import com.SpringBootProject.ExpenseTracker.entity.User;
import com.SpringBootProject.ExpenseTracker.exception.DuplicateResourceException;
import com.SpringBootProject.ExpenseTracker.exception.ResourceNotFoundException;
import com.SpringBootProject.ExpenseTracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "User with email '" + request.getEmail() + "' already exists"
            );
        }
        // NOTE: In a real application with JWT auth, you would hash the password here:
        //   String hashedPassword = passwordEncoder.encode(request.getPassword());
        // For now we store it as plain text — we'll fix this when we add Spring Security.
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // TODO: hash before storing
                .build();

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!existing.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already in use");
        }

        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        existing.setPassword(request.getPassword()); // TODO: hash when adding JWT
        return toResponse(userRepository.save(existing));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        // Because User has CascadeType.ALL on expenses, deleting the user
        // will also delete all their expenses automatically via Hibernate.
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                // Notice: we do NOT include the password field here — by design.
                .build();
    }
}