package com.example.demo.controllers;

import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.dashboard.SellerDashboardOverviewResponse;
import com.example.demo.dtos.responses.dashboard.SellerRevenueResponse;
import com.example.demo.dtos.responses.dashboard.SellerInventoryAlertResponse;
import com.example.demo.services.queries.SellerDashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/seller/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerDashboardController {

    private final SellerDashboardQueryService sellerDashboardQueryService;

    @GetMapping("/overview")
    public ResponseEntity<SuccessResponse<SellerDashboardOverviewResponse>> getSellerDashboardOverview() {
        SellerDashboardOverviewResponse response = sellerDashboardQueryService.getSellerDashboardOverview();
        return ResponseEntity.ok(SuccessResponse.<SellerDashboardOverviewResponse>builder()
                .success(true)
                .message("Get seller dashboard overview successfully")
                .data(response)
                .build());
    }

    @GetMapping("/revenue")
    public ResponseEntity<SuccessResponse<SellerRevenueResponse>> getSellerRevenue(
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        SellerRevenueResponse response = sellerDashboardQueryService.getSellerRevenue(period, startDate, endDate);
        return ResponseEntity.ok(SuccessResponse.<SellerRevenueResponse>builder()
                .success(true)
                .message("Get seller revenue data successfully")
                .data(response)
                .build());
    }

    @GetMapping("/inventory/alerts")
    public ResponseEntity<SuccessResponse<List<SellerInventoryAlertResponse>>> getInventoryAlerts() {
        List<SellerInventoryAlertResponse> response = sellerDashboardQueryService.getInventoryAlerts();
        return ResponseEntity.ok(SuccessResponse.<List<SellerInventoryAlertResponse>>builder()
                .success(true)
                .message("Get seller inventory alerts successfully")
                .data(response)
                .build());
    }
}