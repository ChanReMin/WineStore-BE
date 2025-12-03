package com.example.demo.services.queries;

import com.example.demo.dtos.responses.dashboard.SellerDashboardOverviewResponse;
import com.example.demo.dtos.responses.dashboard.SellerRevenueResponse;
import com.example.demo.dtos.responses.dashboard.SellerInventoryAlertResponse;

import java.time.LocalDate;
import java.util.List;

public interface SellerDashboardQueryService {
    SellerDashboardOverviewResponse getSellerDashboardOverview();
    SellerRevenueResponse getSellerRevenue(String period, LocalDate startDate, LocalDate endDate);
    List<SellerInventoryAlertResponse> getInventoryAlerts();
}
