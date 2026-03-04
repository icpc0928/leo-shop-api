package com.leoshop.service;

import com.leoshop.dto.AdminUserRequest;
import com.leoshop.dto.AdminUserResponse;
import com.leoshop.exception.ResourceNotFoundException;
import com.leoshop.model.AdminUser;
import com.leoshop.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public List<AdminUserResponse> getAll() {
        return adminUserRepository.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    public AdminUserResponse getById(Long id) {
        return AdminUserResponse.from(findById(id));
    }

    @Transactional
    public AdminUserResponse create(AdminUserRequest req) {
        if (adminUserRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        AdminUser admin = AdminUser.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .role(req.getRole() != null ? req.getRole() : "ADMIN")
                .build();

        return AdminUserResponse.from(adminUserRepository.save(admin));
    }

    @Transactional
    public AdminUserResponse update(Long id, AdminUserRequest req) {
        AdminUser admin = findById(id);

        if (req.getEmail() != null && !req.getEmail().equals(admin.getEmail())) {
            if (adminUserRepository.findByEmail(req.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email already exists");
            }
            admin.setEmail(req.getEmail());
        }

        if (req.getName() != null) {
            admin.setName(req.getName());
        }

        if (req.getRole() != null) {
            admin.setRole(req.getRole());
        }

        // Only update password if provided
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        return AdminUserResponse.from(adminUserRepository.save(admin));
    }

    @Transactional
    public void delete(Long id) {
        AdminUser admin = findById(id);
        
        // Prevent deleting the last admin
        long adminCount = adminUserRepository.count();
        if (adminCount <= 1) {
            throw new IllegalStateException("Cannot delete the last admin user");
        }

        adminUserRepository.delete(admin);
    }

    private AdminUser findById(Long id) {
        return adminUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
    }
}
