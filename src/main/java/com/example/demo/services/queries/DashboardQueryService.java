package com.example.demo.services.queries;

import com.example.demo.dtos.responses.dashboard.RevenueAnalyticsResponse;
import com.example.demo.dtos.responses.dashboard.SystemOverviewResponse;
import com.example.demo.dtos.responses.dashboard.UserAnalyticsResponse;

import java.time.LocalDate;

public interface DashboardQueryService {
    SystemOverviewResponse getSystemOverview(LocalDate startDate, LocalDate endDate);
    RevenueAnalyticsResponse getRevenueAnalytics(LocalDate startDate, LocalDate endDate, String groupBy);
    UserAnalyticsResponse getUserAnalytics(LocalDate startDate, LocalDate endDate);
}
