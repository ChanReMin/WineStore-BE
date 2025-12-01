package com.example.demo.controllers;

import com.example.demo.dtos.commands.profile.*;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.profile.*;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.services.commands.ProfileCommandService;
import com.example.demo.services.queries.ProfileQueryService;
import com.example.demo.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profile Management")
public class ProfileController {

    private final ProfileQueryService profileQueryService;
    private final ProfileCommandService profileCommandService;

    /**
     * 1. Get current user profile
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<SuccessResponse<ProfileResponse>> getProfile() {
        Long currentUserId = SecurityUtils.getCurrentUserUuid();

        log.debug("Fetching profile for user ID: {}", currentUserId);

        if (currentUserId == null) {
            throw new BadRequestException("User not authenticated");
        }

        ProfileResponse data = profileQueryService.getProfile(currentUserId);

        return ResponseEntity.ok(
                SuccessResponse.<ProfileResponse>builder()
                        .success(true)
                        .data(data)
                        .build()
        );
    }

    /**
     * 2. Update profile
     */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<SuccessResponse<UpdateProfileResponse>> updateProfile(
            @Valid @ModelAttribute UpdateProfileRequest request
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserUuid();

        if (currentUserId == null) {
            throw new BadRequestException("User not authenticated");
        }

        UpdateProfileResponse data = profileCommandService.updateProfile(currentUserId, request);

        return ResponseEntity.ok(
                SuccessResponse.<UpdateProfileResponse>builder()
                        .success(true)
                        .message("Update profile successful")
                        .data(data)
                        .build()
        );
    }

    /**
     * 3. Change password
     */
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password")
    public ResponseEntity<SuccessResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserUuid();

        if (currentUserId == null) {
            throw new BadRequestException("User not authenticated");
        }

        profileCommandService.changePassword(currentUserId, request);

        return ResponseEntity.ok(
                SuccessResponse.<Void>builder()
                        .success(true)
                        .message("Change password successful")
                        .build()
        );
    }

    /**
     * 5. Get all addresses
     */
    @GetMapping("/addresses")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all user addresses")
    public ResponseEntity<SuccessResponse<AddressListResponse>> getAddresses() {
        Long currentUserId = SecurityUtils.getCurrentUserUuid();

        if (currentUserId == null) {
            throw new BadRequestException("User not authenticated");
        }

        AddressListResponse data = profileQueryService.getAddresses(currentUserId);

        return ResponseEntity.ok(
                SuccessResponse.<AddressListResponse>builder()
                        .success(true)
                        .data(data)
                        .build()
        );
    }

    /**
     * 6. Add new address
     */
    @PostMapping("/addresses")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add new address")
    public ResponseEntity<SuccessResponse<AddAddressResponse>> addAddress(
            @Valid @RequestBody AddAddressRequest request
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserUuid();

        if (currentUserId == null) {
            throw new BadRequestException("User not authenticated");
        }

        AddAddressResponse data = profileCommandService.addAddress(currentUserId, request);

        return ResponseEntity.ok(
                SuccessResponse.<AddAddressResponse>builder()
                        .success(true)
                        .message("Add new address successful")
                        .data(data)
                        .build()
        );
    }

    /**
     * 7. Update address
     */
    @PutMapping("/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update address")
    public ResponseEntity<SuccessResponse<Void>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserUuid();

        if (currentUserId == null) {
            throw new BadRequestException("User not authenticated");
        }

        profileCommandService.updateAddress(currentUserId, addressId, request);

        return ResponseEntity.ok(
                SuccessResponse.<Void>builder()
                        .success(true)
                        .message("Update address successful")
                        .build()
        );
    }

    /**
     * 8. Delete address
     */
    @DeleteMapping("/addresses/{addressId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete address")
    public ResponseEntity<SuccessResponse<Void>> deleteAddress(
            @PathVariable Long addressId
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserUuid();

        if (currentUserId == null) {
            throw new BadRequestException("User not authenticated");
        }

        profileCommandService.deleteAddress(currentUserId, addressId);

        return ResponseEntity.ok(
                SuccessResponse.<Void>builder()
                        .success(true)
                        .message("Delete address successful")
                        .build()
        );
    }

    /**
     * 9. Set default address
     */
    @PutMapping("/addresses/{addressId}/set-default")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Set default address")
    public ResponseEntity<SuccessResponse<Void>> setDefaultAddress(
            @PathVariable Long addressId
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserUuid();

        if (currentUserId == null) {
            throw new BadRequestException("User not authenticated");
        }

        profileCommandService.setDefaultAddress(currentUserId, addressId);

        return ResponseEntity.ok(
                SuccessResponse.<Void>builder()
                        .success(true)
                        .message("Set default address successful")
                        .build()
        );
    }
}
