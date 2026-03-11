package com.leoshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserRequest {
    @NotBlank @Email
    @Size(max = 100)
    private String email;
    @NotBlank
    @Size(min = 8, max = 100, message = "密碼長度需 8~100 字元")
    private String password;
    @NotBlank
    @Size(max = 50)
    private String name;
    private String role;
}
