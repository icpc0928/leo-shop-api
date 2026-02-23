package com.leoshop.controller;

import com.leoshop.dto.*;
import com.leoshop.model.User;
import com.leoshop.repository.UserRepository;
import com.leoshop.security.JwtUtil;
import com.leoshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@RequestHeader("Authorization") String authHeader,
                                                       @RequestBody UpdateProfileRequest request) {
        Long userId = getUserId(authHeader);
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestHeader("Authorization") String authHeader,
                                                @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getUserId(authHeader);
        userService.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    private Long getUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
