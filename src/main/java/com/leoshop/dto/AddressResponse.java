package com.leoshop.dto;

import com.leoshop.model.Address;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private Boolean isDefault;

    public static AddressResponse from(Address a) {
        return AddressResponse.builder()
                .id(a.getId()).name(a.getName()).phone(a.getPhone())
                .address(a.getAddress()).isDefault(a.getIsDefault())
                .build();
    }
}
