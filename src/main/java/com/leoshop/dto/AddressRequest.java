package com.leoshop.dto;

import lombok.Data;

@Data
public class AddressRequest {
    private String name;
    private String phone;
    private String address;
    private Boolean isDefault = false;
}
