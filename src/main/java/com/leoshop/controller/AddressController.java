package com.leoshop.controller;

import com.leoshop.dto.*;
import com.leoshop.model.User;
import com.leoshop.repository.UserRepository;
import com.leoshop.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(Authentication auth) {
        return ResponseEntity.ok(addressService.getAddresses(getUserId(auth)));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(Authentication auth, @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.createAddress(getUserId(auth), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(Authentication auth, @PathVariable Long id, @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(getUserId(auth), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        addressService.deleteAddress(getUserId(auth), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<AddressResponse> setDefault(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(addressService.setDefault(getUserId(auth), id));
    }

    private Long getUserId(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return user.getId();
    }
}
