package com.example.demo.controllers;

import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.dashboard.RevenueAnalyticsResponse;
import com.example.demo.dtos.responses.dashboard.SystemOverviewResponse;
import com.example.demo.dtos.responses.dashboard.UserAnalyticsResponse;
import com.example.demo.services.queries.DashboardQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "APIs for admin dashboard analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardQueryService dashboardQueryService;

    @GetMapping("/overview")
    @Operation(summary = "Get system overview", description = "Get comprehensive system metrics including revenue, orders, users, products and inventory")
    public ResponseEntity<SuccessResponse<SystemOverviewResponse>> getSystemOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        SystemOverviewResponse response = dashboardQueryService.getSystemOverview(startDate, endDate);
        return ResponseEntity.ok(SuccessResponse.<SystemOverviewResponse>builder()
                .success(true)
                .message("Get system overview successfully")
                .data(response)
                .build());

    }

    @GetMapping("/revenue-analytics")
    @Operation(summary = "Get revenue analytics", description = "Get detailed revenue analytics by time, category, region and payment method")
    public ResponseEntity<SuccessResponse<RevenueAnalyticsResponse>> getRevenueAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "day") String groupBy
    ) {
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        RevenueAnalyticsResponse response = dashboardQueryService.getRevenueAnalytics(startDate, endDate, groupBy);
        return ResponseEntity.ok(SuccessResponse.<RevenueAnalyticsResponse>builder()
                .success(true)
                .message("Get revenue analytics successfully")
                .data(response)
                .build());
        }


    @GetMapping("/user-analytics")
    @Operation(summary = "Get user analytics", description = "Get user growth, segments, retention and acquisition channel analytics")
    public ResponseEntity<SuccessResponse<UserAnalyticsResponse>> getUserAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        UserAnalyticsResponse response = dashboardQueryService.getUserAnalytics(startDate, endDate);
        return ResponseEntity.ok(SuccessResponse.<UserAnalyticsResponse>builder()
                .success(true)
                .message("Get user analytics successfully")
                .data(response)
                .build());
    }
}