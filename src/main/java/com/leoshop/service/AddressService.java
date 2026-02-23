package com.leoshop.service;

import com.leoshop.dto.AddressRequest;
import com.leoshop.dto.AddressResponse;
import com.leoshop.exception.BadRequestException;
import com.leoshop.exception.ResourceNotFoundException;
import com.leoshop.model.Address;
import com.leoshop.model.User;
import com.leoshop.repository.AddressRepository;
import com.leoshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public List<AddressResponse> getAddresses(Long userId) {
        return addressRepository.findByUserId(userId).stream().map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefault(userId);
        }
        Address address = Address.builder()
                .user(user).name(request.getName()).phone(request.getPhone())
                .address(request.getAddress()).isDefault(request.getIsDefault())
                .build();
        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(Long userId, Long id, AddressRequest request) {
        Address address = getOwnAddress(userId, id);
        address.setName(request.getName());
        address.setPhone(request.getPhone());
        address.setAddress(request.getAddress());
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefault(userId);
            address.setIsDefault(true);
        }
        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long userId, Long id) {
        Address address = getOwnAddress(userId, id);
        addressRepository.delete(address);
    }

    @Transactional
    public AddressResponse setDefault(Long userId, Long id) {
        clearDefault(userId);
        Address address = getOwnAddress(userId, id);
        address.setIsDefault(true);
        return AddressResponse.from(addressRepository.save(address));
    }

    private Address getOwnAddress(Long userId, Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("Not your address");
        }
        return address;
    }

    private void clearDefault(Long userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(a -> { a.setIsDefault(false); addressRepository.save(a); });
    }
}
