package com.example.demo.services.commands.ServiceCommandImpl;

import com.example.demo.commons.enums.Gender;
import com.example.demo.dtos.commands.profile.*;
import com.example.demo.dtos.responses.profile.*;
import com.example.demo.entities.Account;
import com.example.demo.entities.User;
import com.example.demo.entities.UserAddress;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.commands.AccountCommandRepository;
import com.example.demo.repositories.commands.UserAddressCommandRepository;
import com.example.demo.repositories.commands.UserCommandRepository;
import com.example.demo.repositories.queries.AccountQueryRepository;
import com.example.demo.repositories.queries.UserQueryRepository;
import com.example.demo.services.CloudinaryService;
import com.example.demo.services.commands.ProfileCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileCommandServiceImpl implements ProfileCommandService {

    private final AccountQueryRepository accountQueryRepository;
    private final AccountCommandRepository accountCommandRepository;
    private final UserQueryRepository userQueryRepository;
    private final UserCommandRepository userCommandRepository;
    private final UserAddressCommandRepository userAddressCommandRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    private static final int MAX_ADDRESSES = 10;

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public UpdateProfileResponse updateProfile(Long accountId, UpdateProfileRequest request) {
        log.info("✏️ Updating profile for user: {}", accountId);

        Account account = accountQueryRepository.findByIdWithUser(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User user = account.getUser();

        // Update user fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            user.setGender(Gender.values()[request.getGender()]);
        }

        // Save updated user to Write DB FIRST (fast response)
        User updatedUser = userCommandRepository.save(user);
        log.info("✅ Profile updated successfully for user: {}", accountId);

        return UpdateProfileResponse.builder()
                .id(account.getId())
                .firstName(updatedUser.getFirstName())
                .lastName(updatedUser.getLastName())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public void changePassword(Long accountId, ChangePasswordRequest request) {
        log.debug("Changing password for user: {}", accountId);

        Account account = accountQueryRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate old password
        if (!passwordEncoder.matches(request.getOldPassword(), account.getPasswordHash())) {
            throw new BadRequestException("Old password is incorrect");
        }

        // Check if new password is same as old
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from old password");
        }
        // Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Update password
        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        accountCommandRepository.save(account);

        log.info("Password changed successfully for user: {}", accountId);
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public AddAddressResponse addAddress(Long accountId, AddAddressRequest request) {
        log.debug("Adding address for user: {}", accountId);

        Account account = accountQueryRepository.findByIdWithUser(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User user = account.getUser();

        // Check address limit
        List<UserAddress> existingAddresses = userQueryRepository.findAddressesByUserId(user.getId());
        if (existingAddresses != null && existingAddresses.size() >= MAX_ADDRESSES) {
            throw new BadRequestException("Maximum " + MAX_ADDRESSES + " addresses allowed");
        }

        // If this is the first address or explicitly set as default, make it default
        boolean shouldBeDefault = request.getIsDefault() != null && request.getIsDefault();
        if (existingAddresses == null || existingAddresses.isEmpty()) {
            shouldBeDefault = true;
        }

        // If setting as default, unset other default addresses
        if (shouldBeDefault && existingAddresses != null) {
            existingAddresses.stream()
                    .filter(addr -> addr.getIsDefault() != null && addr.getIsDefault())
                    .forEach(addr -> {
                        addr.setIsDefault(false);
                        userAddressCommandRepository.save(addr);
                    });
        }

        // Create new address
        UserAddress newAddress = UserAddress.builder()
                .user(user)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .state(request.getDistrict())
                .country(request.getCountry())
                .isDefault(shouldBeDefault)
                .build();

        UserAddress savedAddress = userAddressCommandRepository.save(newAddress);

        log.info("Address added successfully for user: {}", accountId);

        return AddAddressResponse.builder()
                .id(savedAddress.getId())
                .fullName(savedAddress.getFullName())
                .isDefault(savedAddress.getIsDefault())
                .createdAt(savedAddress.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public void updateAddress(Long accountId, Long addressId, UpdateAddressRequest request) {
        log.debug("Updating address {} for user: {}", addressId, accountId);

        Account account = accountQueryRepository.findByIdWithUser(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User user = account.getUser();

        UserAddress address = userQueryRepository.findAddressById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Verify ownership
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to update this address");
        }

        // Update fields
        if (request.getFullName() != null) {
            address.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            address.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddressLine() != null) {
            address.setAddressLine(request.getAddressLine());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getDistrict() != null) {
            address.setState(request.getDistrict());
        }
        if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }

        // Handle default flag
        if (request.getIsDefault() != null && request.getIsDefault()) {
            // Unset other default addresses
            List<UserAddress> addresses = userQueryRepository.findAddressesByUserId(user.getId());
            addresses.stream()
                    .filter(addr -> !addr.getId().equals(addressId) &&
                            addr.getIsDefault() != null && addr.getIsDefault())
                    .forEach(addr -> {
                        addr.setIsDefault(false);
                        userAddressCommandRepository.save(addr);
                    });
            address.setIsDefault(true);
        } else if (request.getIsDefault() != null && !request.getIsDefault()) {
            // Don't allow unsetting default if it's the only address or last default
            List<UserAddress> addresses = userQueryRepository.findAddressesByUserId(user.getId());
            long defaultCount = addresses.stream()
                    .filter(addr -> addr.getIsDefault() != null && addr.getIsDefault())
                    .count();

            if (defaultCount == 1 && address.getIsDefault()) {
                throw new BadRequestException("Must have at least one default address");
            }
            address.setIsDefault(false);
        }

        userAddressCommandRepository.save(address);

        log.info("Address {} updated successfully", addressId);
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public void deleteAddress(Long accountId, Long addressId) {
        log.debug("Deleting address {} for user: {}", addressId, accountId);

        Account account = accountQueryRepository.findByIdWithUser(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User user = account.getUser();

        UserAddress address = userQueryRepository.findAddressById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Verify ownership
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to delete this address");
        }

        // Check if deleting the last default address
        List<UserAddress> addresses = userQueryRepository.findAddressesByUserId(user.getId());
        boolean isLastDefault = address.getIsDefault() != null && address.getIsDefault() &&
                addresses.stream()
                        .filter(addr -> addr.getIsDefault() != null && addr.getIsDefault())
                        .count() == 1;

        // Soft delete
        address.softDelete();
        userAddressCommandRepository.save(address);

        // If deleted the last default, set another address as default
        if (isLastDefault) {
            addresses.stream()
                    .filter(addr -> !addr.getId().equals(addressId) && !addr.isDeleted())
                    .findFirst()
                    .ifPresent(addr -> {
                        addr.setIsDefault(true);
                        userAddressCommandRepository.save(addr);
                    });
        }

        log.info("Address {} deleted successfully", addressId);
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public void setDefaultAddress(Long accountId, Long addressId) {
        log.debug("Setting default address {} for user: {}", addressId, accountId);

        Account account = accountQueryRepository.findByIdWithUser(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User user = account.getUser();

        UserAddress address = userQueryRepository.findAddressById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // Verify ownership
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You don't have permission to modify this address");
        }

        // Unset all other default addresses
        List<UserAddress> addresses = userQueryRepository.findAddressesByUserId(user.getId());
        addresses.stream()
                .filter(addr -> addr.getIsDefault() != null && addr.getIsDefault())
                .forEach(addr -> {
                    addr.setIsDefault(false);
                    userAddressCommandRepository.save(addr);
                });

        // Set this address as default
        address.setIsDefault(true);
        userAddressCommandRepository.save(address);

        log.info("Address {} set as default successfully", addressId);
    }
}
