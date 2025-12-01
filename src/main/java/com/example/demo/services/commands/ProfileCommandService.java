package com.example.demo.services.commands;

import com.example.demo.dtos.commands.profile.*;
import com.example.demo.dtos.responses.profile.*;

public interface ProfileCommandService {
    UpdateProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    void changePassword(Long userId, ChangePasswordRequest request);
    AddAddressResponse addAddress(Long userId, AddAddressRequest request);
    void updateAddress(Long userId, Long addressId, UpdateAddressRequest request);
    void deleteAddress(Long userId, Long addressId);
    void setDefaultAddress(Long userId, Long addressId);
}
