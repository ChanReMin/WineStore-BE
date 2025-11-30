package com.example.demo.services;

import com.example.demo.commons.enums.AccountRole;
import com.example.demo.commons.enums.AccountStatus;
import com.example.demo.dtos.commands.user.ExportUsersRequest;
import com.example.demo.dtos.commands.user.ExportUsersFilters;
import com.example.demo.entities.Account;
import com.example.demo.repositories.queries.AccountQueryRepository;
import com.example.demo.utils.AccountSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final AccountQueryRepository accountQueryRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Export users to CSV format
     */
    public byte[] exportToCSV(ExportUsersRequest request) throws IOException {
        List<Account> accounts = fetchAccountsForExport(request);

        StringBuilder csv = new StringBuilder();

        // Add BOM for UTF-8 (helps Excel recognize UTF-8)
        csv.append("\uFEFF");

        // CSV Header
        csv.append("Email,Name,Phone,Role,Status,Created At\n");

        // CSV Data
        for (Account account : accounts) {
            csv.append(escapeCSV(account.getEmail())).append(",");
            csv.append(escapeCSV(getFullName(account))).append(",");
            csv.append(escapeCSV(account.getUser().getPhoneNumber())).append(",");
            csv.append(escapeCSV(account.getRole().name().toLowerCase())).append(",");
            csv.append(escapeCSV(account.getStatus().name().toLowerCase())).append(",");
            csv.append(escapeCSV(account.getCreatedAt().format(DATE_FORMATTER)));
            csv.append("\n");
        }

        // Add BOM for UTF-8 (Excel needs this to recognize UTF-8)
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        // Combine BOM + CSV content
        byte[] result = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(csvBytes, 0, result, bom.length, csvBytes.length);

        return result;
    }

    /**
     * Fetch accounts based on export filters
     */
    private List<Account> fetchAccountsForExport(ExportUsersRequest request) {
        ExportUsersFilters filters = request.getFilters();

        // Parse filters
        String search = filters != null ? filters.getSearch() : null;
        AccountRole role = parseRole(filters != null ? filters.getRole() : null);
        AccountStatus status = parseStatus(filters != null ? filters.getStatus() : null);

        // Build specification with filters
        Specification<Account> spec = AccountSpecification.withFilters(search, role, status);

        // Add date range filter if provided
        if (filters != null && filters.getStartDate() != null && filters.getEndDate() != null) {
            LocalDateTime startDateTime = filters.getStartDate().atStartOfDay();
            LocalDateTime endDateTime = filters.getEndDate().atTime(23, 59, 59);

            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("createdAt"), startDateTime, endDateTime)
            );
        }

        // Fetch with pagination (limit to 10000 records for safety)
        Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Account> accountPage = accountQueryRepository.findAll(spec, pageable);

        log.info("Exporting {} users", accountPage.getTotalElements());

        return accountPage.getContent();
    }

    /**
     * Parse role from string
     */
    private AccountRole parseRole(String role) {
        if (role == null || role.trim().isEmpty()) return null;

        try {
            // Support both "buyer"/"customer" and "BUYER"/"CUSTOMER"
            String upperRole = role.toUpperCase();
            if ("BUYER".equals(upperRole)) {
                return AccountRole.CUSTOMER;
            }
            return AccountRole.valueOf(upperRole);
        } catch (Exception e) {
            log.warn("Invalid role: {}", role);
            return null;
        }
    }

    /**
     * Parse status from string
     */
    private AccountStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) return null;

        try {
            return AccountStatus.fromString(status);
        } catch (Exception e) {
            log.warn("Invalid status: {}", status);
            return null;
        }
    }

    /**
     * Get full name from account
     */
    private String getFullName(Account account) {
        if (account.getUser() == null) return "";

        String firstName = account.getUser().getFirstName();
        String lastName = account.getUser().getLastName();

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return firstName != null ? firstName : "";
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCSV(String value) {
        if (value == null) return "";

        // Trim whitespace to avoid issues
        value = value.trim();

        // If contains comma, quote, newline, or carriage return -> wrap in quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            // Escape quotes by doubling them
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}