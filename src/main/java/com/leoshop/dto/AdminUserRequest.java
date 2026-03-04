package com.leoshop.dto;

import lombok.Data;

@Data
public class AdminUserRequest {
    private String email;
    private String password;
    private String name;
    private String role;
}
