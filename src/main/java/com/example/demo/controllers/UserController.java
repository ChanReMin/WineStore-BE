package com.example.demo.controllers;

import com.example.demo.dtos.commands.user.*;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.user.*;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.services.commands.UserCommandService;
import com.example.demo.services.queries.UserQueryService;
import com.example.demo.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "User Management")
public class UserController {
    
    private final UserQueryService userQueryService;
    private final UserCommandService userCommandService;
    /**
     * 1. Get users list
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin only")
    public ResponseEntity<SuccessResponse<UserListResponse>> getUsersList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        UserListResponse data = userQueryService.getUsersList(
                page, limit, search, role, status, sortBy, sortOrder
        );

        return ResponseEntity.ok(
                SuccessResponse.<UserListResponse>builder()
                        .success(true)
                        .message("Get users list successfully")
                        .data(data)
                        .build()
        );
    }

    /**
     * 2. Get user detail
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    @Operation(summary = "Admin only")
    public ResponseEntity<SuccessResponse<UserDetailResponse>> getUserDetail(
            @PathVariable Long userId
    ) {
        UserDetailResponse data = userQueryService.getUserDetail(userId);

        return ResponseEntity.ok(
                SuccessResponse.<UserDetailResponse>builder()
                        .success(true)
                        .message("Get user detail successfully")
                        .data(data)
                        .build()
        );
    }

    /**
     * 9. Get user statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<UserStatisticsResponse>> getUserStatistics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        UserStatisticsResponse data = userQueryService.getUserStatistics(startDate, endDate);
        return ResponseEntity.ok(
                SuccessResponse.<UserStatisticsResponse>builder()
                        .success(true)
                        .message("Get user statistics successfully")
                        .data(data)
                        .build()
        );
    }

    /**
     * 10. Export users
     */
    @PostMapping("/export")
    @Operation(summary = "Export users to file - Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportUsers(
            @Valid @RequestBody ExportUsersRequest request
    ) throws IOException {
        ExportUsersResponse exportData = userQueryService.exportUsers(request);

        // Determine content type based on format
        String contentType;
        String filename;

        switch (request.getFormat().toLowerCase()) {
            case "csv":
                contentType = "text/csv";
                filename = "users_export_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
                break;
            default:
                throw new BadRequestException("Unsupported format: " + request.getFormat());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(exportData.getFileContent().length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(exportData.getFileContent());
    }

    // ==================== COMMAND OPERATIONS (Write) ====================

    /**
     * 3. Update user
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<UserDetailResponse>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        userCommandService.updateUser(userId, request);

        // Query the updated user
        UserDetailResponse data = userQueryService.getUserDetail(userId);

        return ResponseEntity.ok(
                SuccessResponse.<UserDetailResponse>builder()
                        .success(true)
                        .message("User updated successfully")
                        .data(data)
                        .build()
        );
    }

    /**
     * 4. Change user status
     */
    @PatchMapping("/{userId}/status")
    @Operation(summary = "Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<ChangeUserStatusResponse>> changeUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody ChangeUserStatusRequest request
    ) {
        ChangeUserStatusResponse data = userCommandService.changeUserStatus(userId, request);

        return ResponseEntity.ok(
                SuccessResponse.<ChangeUserStatusResponse>builder()
                        .success(true)
                        .message("User status changed successfully")
                        .data(data)
                        .build()
        );
    }

    /**
     * 5. Delete user
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<Void>> deleteUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") boolean permanent
    ) {
        userCommandService.deleteUser(userId, permanent);

        return ResponseEntity.ok(
                SuccessResponse.<Void>builder()
                        .success(true)
                        .message("User deleted successfully")
                        .build()
        );
    }

    /**
     * 6. Restore user
     */
    @PostMapping("/{userId}/restore")
    @Operation(summary = "Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<RestoreUserResponse>> restoreUser(
            @PathVariable Long userId
    ) {
        RestoreUserResponse data = userCommandService.restoreUser(userId);

        return ResponseEntity.ok(
                SuccessResponse.<RestoreUserResponse>builder()
                        .success(true)
                        .message("User restored successfully")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("/bulk-actions")
    @Operation(summary = "Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<BulkActionResponse>> bulkActions(
            @Valid @RequestBody BulkActionRequest request
    ) {
        BulkActionResponse data = userCommandService.bulkActions(request);

        return ResponseEntity.ok(
                SuccessResponse.<BulkActionResponse>builder()
                        .success(true)
                        .message("Bulk action completed successfully")
                        .data(data)
                        .build()
        );
    }

    @PatchMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update own avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponse<UpdateAvatarResponse>> updateOwnAvatar(
            @ModelAttribute UpdateAvatarRequest avatar
    ) {
        Long currentUserId = SecurityUtils.getCurrentUserUuid();

        if (currentUserId == null) {
            throw new BadRequestException("User not authenticated");
        }

        UpdateAvatarResponse data = userCommandService.updateAvatar(currentUserId, avatar);

        return ResponseEntity.ok(
                SuccessResponse.<UpdateAvatarResponse>builder()
                        .success(true)
                        .message("Avatar updated successfully")
                        .data(data)
                        .build()
        );
    }
}