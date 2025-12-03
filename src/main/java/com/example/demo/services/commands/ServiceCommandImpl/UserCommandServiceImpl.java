package com.example.demo.services.commands.ServiceCommandImpl;

import com.example.demo.commons.enums.AccountRole;
import com.example.demo.commons.enums.AccountStatus;
import com.example.demo.commons.enums.NotificationStatus;
import com.example.demo.dtos.commands.user.*;
import com.example.demo.dtos.notifications.NotificationMessage;
import com.example.demo.dtos.responses.user.*;
import com.example.demo.entities.Account;
import com.example.demo.entities.Notification;
import com.example.demo.entities.User;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.commands.AccountCommandRepository;
import com.example.demo.repositories.commands.NotificationCommandRepository;
import com.example.demo.repositories.commands.UserCommandRepository;
import com.example.demo.repositories.queries.AccountQueryRepository;
import com.example.demo.services.CloudinaryService;
import com.example.demo.services.commands.UserCommandService;
import com.example.demo.services.notifications.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCommandServiceImpl implements UserCommandService {
    private final AccountCommandRepository accountCommandRepository;
    private final AccountQueryRepository accountQueryRepository;
    private final NotificationCommandRepository notificationCommandRepository;
    private final CloudinaryService cloudinaryService;
    private final UserCommandRepository userCommandRepository;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public ChangeUserStatusResponse changeUserStatus(Long userId, ChangeUserStatusRequest request) {
        Account account = accountQueryRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateNotAdmin(account);

        AccountStatus newStatus = AccountStatus.fromString(request.getStatus());
        account.setStatus(newStatus);
        account = accountCommandRepository.save(account);

        if (request.getReason() != null) {
            log.info("User {} status changed to {} - Reason: {}",
                    userId, newStatus, request.getReason());
        }

        return ChangeUserStatusResponse.builder()
                .id(account.getId().toString())
                .status(account.getStatus().name().toLowerCase())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public ChangeUserRoleResponse changeUserRole(Long userId, ChangeUserRoleRequest request) {
        Account account = accountQueryRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateNotAdmin(account);

        AccountRole newRole = AccountRole.fromString(request.getRole());
        account.setRole(newRole);
        account = accountCommandRepository.save(account);

        if (request.getReason() != null) {
            log.info("User {} role changed to {} - Reason: {}",
                    userId, newRole, request.getReason());
        }

        return ChangeUserRoleResponse.builder()
                .id(account.getId().toString())
                .role(account.getRole().name().toLowerCase())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public void deleteUser(Long userId, boolean permanent) {
        Account account = accountQueryRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateNotAdmin(account);

        if (permanent) {
            accountCommandRepository.delete(account);
            log.info("Permanently deleted user: {}", userId);
        } else {
            accountCommandRepository.softDelete(userId);
            log.info("Soft deleted user: {}", userId);
        }
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public RestoreUserResponse restoreUser(Long userId) {
        Account account = accountQueryRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        account.setStatus(AccountStatus.ACTIVE);
        account = accountCommandRepository.save(account);

        log.info("Restored user: {}", userId);

        return RestoreUserResponse.builder()
                .id(account.getId().toString())
                .status(account.getStatus().name().toLowerCase())
                .restoredAt(LocalDateTime.now())
                .build();
    }


    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public BulkActionResponse bulkActions(BulkActionRequest request) {
        List<Long> userIds = request.getUserIds().stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // Batch fetch all accounts with users in ONE query
        List<Account> accounts = accountQueryRepository.findAllByIdWithUser(userIds);

        // Validate all are not admin
        accounts.forEach(this::validateNotAdmin);

        Map<Long, Account> accountMap = accounts.stream()
                .collect(Collectors.toMap(Account::getId, a -> a));

        List<BulkActionResponse.BulkResult> results = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        String action = request.getAction();

        try {
            switch (action) {
                case "update_status":
                    successCount = handleBulkUpdateStatus(userIds, accountMap, request.getData(), results);
                    break;

                case "update_role":
                    successCount = handleBulkUpdateRole(userIds, accountMap, request.getData(), results);
                    break;

                case "delete":
                    successCount = handleBulkDelete(userIds, accountMap, request.getData(), results);
                    break;

                default:
                    throw new BadRequestException("Invalid action: " + action);
            }

            failedCount = userIds.size() - successCount;

        } catch (Exception e) {
            log.error("Bulk action failed: {}", e.getMessage());
            throw e;
        }

        log.info("Bulk action {} completed: {} success, {} failed", action, successCount, failedCount);

        return BulkActionResponse.builder()
                .successCount(successCount)
                .failedCount(failedCount)
                .results(results)
                .build();
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public UpdateAvatarResponse updateAvatar(Long userId, UpdateAvatarRequest avatarFile) {
        log.debug("Updating avatar for user: {}", userId);

        Account account = accountCommandRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User user = account.getUser();
        if (user == null) {
            throw new ResourceNotFoundException("User profile not found");
        }

        if (user.getAvatar() != null && !user.getAvatar().isEmpty()
                && !user.getAvatar().contains("placeholder")) {
            cloudinaryService.deleteImage(user.getAvatar());
        }

        // Upload new avatar
        String newAvatarUrl = cloudinaryService.uploadImage(avatarFile.getAvatar());

        // Update user avatar
        user.setAvatar(newAvatarUrl);
        userCommandRepository.save(user);

        log.info("Avatar updated successfully for user: {}", userId);

        return UpdateAvatarResponse.builder()
                .avatarUrl(newAvatarUrl)
                .message("Avatar updated successfully")
                .build();
    }

    @Override
    @Transactional(transactionManager = "writeTransactionManager")
    public SellerRequestResponse createSellerRequest(Long userId, CreateSellerRequest request) {
        User user = userCommandRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getAccount().getRole() == AccountRole.SELLER || user.getAccount().getRole() == AccountRole.ADMIN) {
            throw new BadRequestException("Your account is already a seller or admin");
        }

        String message = String.format(
                    "User %s %s (Email: %s) sent a request to become a seller.%s",
                user.getFirstName() != null ? user.getFirstName() : "",
                user.getLastName() != null ? user.getLastName() : "",
                user.getAccount().getEmail(),
                request.getReason() != null && !request.getReason().isEmpty()
                        ? "\nReason: " + request.getReason()
                        : "");

        //Send notifications to admin
        Long adminId = accountQueryRepository.findFirstAdminId()
                .orElseThrow(() -> new IllegalStateException("No admin account found"));
            NotificationMessage msg = NotificationMessage.builder()
                    .id(0L)
                    .userId(adminId)      // ID của từng admin
                    .title("Seller request")
                    .message("Request to become a seller from " + user.getAccount().getEmail())
                    .status(NotificationStatus.SUCCESS)
                    .itemUrl("https://example.com/test/item/123")
                    .createdAt(Instant.now())
                    .build();
            notificationProducer.send(msg);

        Notification userNotification = Notification.builder()
                .user(user)
                .title("Request to become a seller")
                .message("Your request to become a seller has been successfully sent and is waiting for admin to process.")
                .status(NotificationStatus.SUCCESS)
                .isRead(false)
                .build();

        Notification savedNotification = notificationCommandRepository.save(userNotification);

        return SellerRequestResponse.builder()
                .requestId(savedNotification.getId())
                .status("pending")
                .createdAt(savedNotification.getCreatedAt())
                .build();
    }

    // Private helper methods for bulk actions

    private int handleBulkUpdateStatus(List<Long> userIds, Map<Long, Account> accountMap,
                                       Map<String, Object> data, List<BulkActionResponse.BulkResult> results) {
        String status = (String) data.get("status");
        AccountStatus newStatus = AccountStatus.fromString(status);

        int successCount = accountCommandRepository.batchUpdateStatus(userIds, newStatus);

        userIds.forEach(userId -> results.add(BulkActionResponse.BulkResult.builder()
                .userId(userId.toString())
                .success(true)
                .build()));

        return successCount;
    }

    private int handleBulkUpdateRole(List<Long> userIds, Map<Long, Account> accountMap,
                                     Map<String, Object> data, List<BulkActionResponse.BulkResult> results) {
        String role = (String) data.get("role");
        AccountRole newRole = parseRole(role);

        int successCount = accountCommandRepository.batchUpdateRole(userIds, newRole);

        userIds.forEach(userId -> results.add(BulkActionResponse.BulkResult.builder()
                .userId(userId.toString())
                .success(true)
                .build()));

        return successCount;
    }

    private int handleBulkDelete(List<Long> userIds, Map<Long, Account> accountMap,
                                 Map<String, Object> data, List<BulkActionResponse.BulkResult> results) {
        Boolean permanent = (Boolean) data.getOrDefault("permanent", false);

        int successCount;
        if (permanent) {
            List<Account> accountsToDelete = new ArrayList<>(accountMap.values());
            accountCommandRepository.deleteAll(accountsToDelete);
            successCount = accountsToDelete.size();
        } else {
            successCount = accountCommandRepository.batchSoftDelete(userIds);
        }

        userIds.forEach(userId -> results.add(BulkActionResponse.BulkResult.builder()
                .userId(userId.toString())
                .success(true)
                .build()));

        return successCount;
    }

    // Helper methods

    private void validateNotAdmin(Account account) {
        if (account.getRole() == AccountRole.ADMIN) {
            throw new BadRequestException("Cannot perform action on admin user");
        }
    }

    private AccountRole parseRole(String role) {
        if ("customer".equalsIgnoreCase(role)) {
            return AccountRole.CUSTOMER;
        }
        return AccountRole.fromString(role);
    }
}
