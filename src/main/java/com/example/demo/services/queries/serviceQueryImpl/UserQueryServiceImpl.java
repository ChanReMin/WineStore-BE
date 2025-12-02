package com.example.demo.services.queries.serviceQueryImpl;

import com.example.demo.commons.enums.AccountRole;
import com.example.demo.commons.enums.AccountStatus;
import com.example.demo.dtos.commands.user.ExportUsersRequest;
import com.example.demo.dtos.responses.user.*;
import com.example.demo.entities.Account;
import com.example.demo.entities.User;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.queries.AccountQueryRepository;
import com.example.demo.repositories.queries.UserQueryRepository;
import com.example.demo.services.ExportService;
import com.example.demo.services.queries.UserQueryService;
import com.example.demo.utils.AccountSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserQueryServiceImpl implements UserQueryService {
    private final UserQueryRepository userQueryRepository;
    private final AccountQueryRepository accountQueryRepository;
    private final ExportService exportService;

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public UserListResponse getUsersList(
            Integer page,
            Integer limit,
            String search,
            String role,
            String status,
            String sortBy,
            String sortOrder
    ) {
        log.debug("Getting users list with params: page={}, limit={}, search={}, role={}, status={}",
                page, limit, search, role, status);

        if (page < 1) {
            throw new IllegalArgumentException("Page must be >= 1");
        }
        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be >= 1");
        }

        // Validate role
        AccountRole roleEnum = null;
        if (role != null) {
            try {
                roleEnum = AccountRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid role value: " + role);
            }
        }

        // Validate status
        AccountStatus statusEnum = null;
        if (status != null) {
            try {
                statusEnum = AccountStatus.fromString(status);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid status value: " + status);
            }
        }

        log.info("Parsed role: {}, status: {}", roleEnum, statusEnum);

        Pageable pageable = PageRequest.of(
                page - 1,
                limit,
                buildSort(sortBy, sortOrder)
        );

//        Page<Account> accountPage = accountQueryRepository.findUsersWithFilters(
//                search, roleEnum, statusEnum, pageable
//        );
        Page<Account> accountPage = accountQueryRepository.findAll(
                AccountSpecification.withFilters(search, roleEnum, statusEnum),
                pageable
        );


        if (accountPage.isEmpty()) {
            return emptyResponse(page, limit);
        }

        List<Account> accounts = accountPage.getContent();

        // Prepare lists
        List<Long> userIds = accounts.stream().map(a -> a.getUser().getId()).toList();
        List<Long> accountIds = accounts.stream().map(Account::getId).toList();

        // Batch queries
        Map<Long, Integer> orderCountMap = buildOrderCountMap(userIds);
        Map<Long, Long> totalSpentMap = buildTotalSpentMap(userIds);
        Map<Long, Integer> productCountMap = buildProductCountMap(accountIds);

        // Map responses
        List<UserListResponse.UserSummary> users = accounts.stream()
                .map(acc -> mapToUserSummary(acc, orderCountMap, totalSpentMap, productCountMap))
                .toList();

        return UserListResponse.builder()
                .users(users)
                .pagination(buildPagination(page, limit, accountPage))
                .build();
    }

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public UserDetailResponse getUserDetail(Long userId) {
        log.debug("Getting user detail: {}", userId);

        // Single query with all JOIN FETCH
        Account account = accountQueryRepository.findByIdWithUserAndAddresses(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (account.getRole() == AccountRole.ADMIN) {
            throw new BadRequestException("Cannot view admin user details");
        }

        User user = account.getUser();

        // Get statistics (3 individual queries - acceptable for detail page)
        Integer totalOrders = userQueryRepository.countOrdersByUserId(user.getId());
        Long totalSpent = userQueryRepository.sumTotalSpentByUserId(user.getId());

        // Only get product count for SELLER role
        // Uses accountId because Product.createdBy is Account, not User
        Integer totalProducts = account.getRole() == AccountRole.SELLER
                ? userQueryRepository.countProductsByAccountId(account.getId())
                : 0;

        UserDetailResponse.Statistics statistics = UserDetailResponse.Statistics.builder()
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .totalProducts(totalProducts)
                .build();

        // Get default address (already loaded with JOIN FETCH)
        UserDetailResponse.Address address = null;
        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            var defaultAddress = user.getAddresses().stream()
                    .filter(a -> a.getIsDefault() != null && a.getIsDefault())
                    .findFirst()
                    .orElse(user.getAddresses().get(0));

            address = UserDetailResponse.Address.builder()
                    .street(defaultAddress.getAddressLine())
                    .city(defaultAddress.getCity())
                    .country(defaultAddress.getCountry())
                    .build();
        }

        return UserDetailResponse.builder()
                .id(account.getId().toString())
                .email(account.getEmail())
                .name(getFullName(user))
                .phone(user.getPhoneNumber())
                .role(account.getRole().name().toLowerCase())
                .status(account.getStatus().name().toLowerCase())
                .avatar(user.getAvatar())
                .emailVerified(true)
                .address(address)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .lastLogin(account.getLastLoginAt())
                .statistics(statistics)
                .notes(null)
                .build();
    }

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public UserStatisticsResponse getUserStatistics(LocalDate startDate, LocalDate endDate) {
        log.debug("Getting user statistics: startDate={}, endDate={}", startDate, endDate);

        // Default to current month if dates not provided
        if (startDate == null || endDate == null) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1);
            endDate = currentMonth.atEndOfMonth();
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // All aggregate queries - no N+1 issue
        Long totalUsers = accountQueryRepository.countAllNonAdminUsers();
        Long activeUsers = accountQueryRepository.countByStatus(AccountStatus.ACTIVE);
        Long inactiveUsers = accountQueryRepository.countByStatus(AccountStatus.INACTIVE);
        Long bannedUsers = accountQueryRepository.countByStatus(AccountStatus.LOCKED);

        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        Long newUsersThisMonth = accountQueryRepository.countNewUsersThisMonth(startOfMonth);

        // Get users by role
        List<Object[]> roleResults = accountQueryRepository.countByRole();

        // Initialize with 0
        Long buyerCount = 0L;
        Long sellerCount = 0L;

        // Map results to specific roles
        for (Object[] result : roleResults) {
            AccountRole role = (AccountRole) result[0];
            Long count = (Long) result[1];

            switch (role) {
                case CUSTOMER:
                    buyerCount = count;
                    break;
                case SELLER:
                    sellerCount = count;
                    break;
            }
        }

        UserStatisticsResponse.UsersByRole usersByRole = UserStatisticsResponse.UsersByRole.builder()
                .customer(buyerCount)
                .seller(sellerCount)
                .build();

        // Get user growth
        List<Object[]> growthResults = accountQueryRepository.getUserGrowth(startDateTime, endDateTime);
        List<UserStatisticsResponse.UserGrowth> userGrowth = growthResults.stream()
                .map(result -> UserStatisticsResponse.UserGrowth.builder()
                        .date(((java.sql.Date) result[0]).toLocalDate())
                        .count((Long) result[1])
                        .build())
                .collect(Collectors.toList());

        return UserStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .bannedUsers(bannedUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .usersByRole(usersByRole)
                .userGrowth(userGrowth)
                .build();
    }

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public ExportUsersResponse exportUsers(ExportUsersRequest request) {
        log.debug("Exporting users: format={}", request.getFormat());

        if(request.getFilters().getStartDate().isAfter(request.getFilters().getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        try {
            byte[] fileContent;

            switch (request.getFormat().toLowerCase()) {
                case "csv":
                    fileContent = exportService.exportToCSV(request);
                    break;
                default:
                    throw new BadRequestException("Unsupported format: " + request.getFormat());
            }

            Long totalRecords = accountQueryRepository.countAllNonAdminUsers();

            return ExportUsersResponse.builder()
                    .fileContent(fileContent)
                    .totalRecords(totalRecords)
                    .filename("users_export." + request.getFormat())
                    .build();

        } catch (IOException e) {
            log.error("Failed to export users", e);
            throw new RuntimeException("Failed to export users: " + e.getMessage());
        }
    }


    private UserListResponse.UserSummary mapToUserSummary(
            Account account,
            Map<Long, Integer> orderCountMap,
            Map<Long, Long> totalSpentMap,
            Map<Long, Integer> productCountMap
    ) {
        User user = account.getUser();
        Long userId = user.getId();
        Long accountId = account.getId();

        // Get product count only for sellers
        Integer totalProducts = account.getRole() == AccountRole.SELLER
                ? productCountMap.getOrDefault(accountId, 0)
                : 0;

        return UserListResponse.UserSummary.builder()
                .id(account.getId().toString())
                .email(account.getEmail())
                .name(getFullName(user))
                .phone(user.getPhoneNumber())
                .role(account.getRole().name().toLowerCase())
                .status(account.getStatus().name().toLowerCase())
                .avatar(user.getAvatar())
                .emailVerified(true)
                .createdAt(account.getCreatedAt())
                .lastLogin(account.getLastLoginAt())
                .totalOrders(orderCountMap.getOrDefault(userId, 0))
                .totalSpent(totalSpentMap.getOrDefault(userId, 0L))
                .build();
    }

    private String getFullName(User user) {
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        }
        return user.getFirstName() != null ? user.getFirstName() : "";
    }

    private AccountRole parseRole(String value) {
        if (value == null) return null;
        try { return AccountRole.valueOf(value.toUpperCase()); }
        catch (Exception e) { return null; }
    }

    private AccountStatus parseStatus(String value) {
        if (value == null) return null;
        try { return AccountStatus.fromString(value); }
        catch (Exception e) { return null; }
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        String field = switch (sortBy == null ? "" : sortBy) {
            case "name" -> "user.firstName";
            case "email" -> "email";
            default -> "createdAt";
        };

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }

    private Map<Long, Integer> buildOrderCountMap(List<Long> userIds) {
        return userQueryRepository.countOrdersByUserIds(userIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).longValue(),
                        r -> ((Number) r[1]).intValue()
                ));
    }

    private Map<Long, Long> buildTotalSpentMap(List<Long> userIds) {
        return userQueryRepository.sumTotalSpentByUserIds(userIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).longValue(),
                        r -> ((Number) r[1]).longValue()
                ));
    }

    private Map<Long, Integer> buildProductCountMap(List<Long> accountIds) {
        return userQueryRepository.countProductsByAccountIds(accountIds)
                .stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).longValue(),
                        r -> ((Number) r[1]).intValue()
                ));
    }

    private UserListResponse.Pagination buildPagination(
            Integer page, Integer limit, Page<Account> accountPage
    ) {
        return UserListResponse.Pagination.builder()
                .currentPage(page)
                .totalPages(accountPage.getTotalPages())
                .totalUsers(accountPage.getTotalElements())
                .limit(limit)
                .build();
    }

    private UserListResponse emptyResponse(Integer page, Integer limit) {
        return UserListResponse.builder()
                .users(Collections.emptyList())
                .pagination(UserListResponse.Pagination.builder()
                        .currentPage(page)
                        .totalPages(0)
                        .totalUsers(0L)
                        .limit(limit)
                        .build())
                .build();
    }

}
