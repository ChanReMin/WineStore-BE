package com.example.demo.services.queries.serviceQueryImpl;
import com.example.demo.commons.enums.AccountRole;
import com.example.demo.commons.enums.OrderStatus;
import com.example.demo.commons.enums.ProductStatus;
import com.example.demo.dtos.responses.dashboard.*;
import com.example.demo.services.queries.DashboardQueryService;
import com.example.demo.repositories.queries.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardQueryServiceImpl implements DashboardQueryService {

    private final OrderQueryRepository orderQueryRepository;
    private final UserQueryRepository userQueryRepository;
    private final AccountQueryRepository accountQueryRepository;
    private final ProductQueryRepository productQueryRepository;
    private final InventoryQueryRepository inventoryQueryRepository;
    private final WarehouseQueryRepository warehouseQueryRepository;
    private final PaymentTransactionQueryRepository paymentTransactionQueryRepository;

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public SystemOverviewResponse getSystemOverview(LocalDate startDate, LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Business Metrics
        BusinessMetricsDTO businessMetrics = calculateBusinessMetrics(startDateTime, endDateTime);

        // Users
        UsersDTO users = calculateUsersMetrics(startDateTime, endDateTime);

        // Products
        ProductsDTO products = calculateProductsMetrics();

        // Orders
        OrdersDTO orders = calculateOrdersMetrics(startDateTime, endDateTime);

        // Inventory
        InventoryDTO inventory = calculateInventoryMetrics();


        return SystemOverviewResponse.builder()
                .period(new PeriodDTO(startDate, endDate))
                .businessMetrics(businessMetrics)
                .users(users)
                .products(products)
                .orders(orders)
                .inventory(inventory)
                .build();
    }

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public RevenueAnalyticsResponse getRevenueAnalytics(LocalDate startDate, LocalDate endDate, String groupBy) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Revenue Chart
        List<RevenueChartDTO> revenueChart = calculateRevenueChart(startDateTime, endDateTime, groupBy);

        // Payment Methods
        Map<String, PaymentMethodStatsDTO> paymentMethods = calculatePaymentMethodStats(startDateTime, endDateTime);

        // Categories Performance
        List<CategoryPerformanceDTO> categoriesPerformance = calculateCategoriesPerformance(startDateTime, endDateTime);

        // Regions Performance
        List<RegionPerformanceDTO> regionsPerformance = calculateRegionsPerformance(startDateTime, endDateTime);

        return RevenueAnalyticsResponse.builder()
                .revenueChart(revenueChart)
                .paymentMethods(paymentMethods)
                .categoriesPerformance(categoriesPerformance)
                .regionsPerformance(regionsPerformance)
                .build();
    }

    @Override
    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public UserAnalyticsResponse getUserAnalytics(LocalDate startDate, LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be after endDate");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // User Growth
        List<UserGrowthDTO> userGrowth = calculateUserGrowthDay(startDateTime, endDateTime);

        // User Segments
        Map<String, UserSegmentDTO> userSegments = calculateUserSegments();

        return UserAnalyticsResponse.builder()
                .userGrowth(userGrowth)
                .userSegments(userSegments)
                .build();
    }

    // Private helper methods

    private BusinessMetricsDTO calculateBusinessMetrics(LocalDateTime start, LocalDateTime end) {
        // Total Revenue
        BigDecimal totalRevenue = orderQueryRepository.calculateTotalRevenue(start, end);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // Gross Profit (assuming 30% profit margin)
        BigDecimal grossProfit = totalRevenue.multiply(new BigDecimal("0.30"));

        // Profit Margin
        BigDecimal profitMarginPercent = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? new BigDecimal("30.0")
                : BigDecimal.ZERO;

        // Total Orders
        Long totalOrders = orderQueryRepository.countOrdersByDateRange(start, end);

        // Average Order Value
        BigDecimal averageOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Conversion Rate (mock - implement based on your tracking)
        BigDecimal conversionRate = new BigDecimal("3.2");

        return BusinessMetricsDTO.builder()
                .totalRevenue(totalRevenue)
                .grossProfit(grossProfit)
                .profitMarginPercent(profitMarginPercent)
                .totalOrders(totalOrders)
                .averageOrderValue(averageOrderValue)
                .conversionRate(conversionRate)
                .build();
    }

    private UsersDTO calculateUsersMetrics(LocalDateTime start, LocalDateTime end) {
        Long totalUsers = userQueryRepository.count();
        Long newThisMonth = userQueryRepository.countByCreatedAtBetween(start, end);
        Long activeUsers = userQueryRepository.countActiveUsers(start, end);

        // Count by role
        Long customers = accountQueryRepository.countByRoleDashboard(AccountRole.CUSTOMER);
        Long sellers = accountQueryRepository.countByRoleDashboard(AccountRole.SELLER);
        Long admins = accountQueryRepository.countByRoleDashboard(AccountRole.ADMIN);
        Long pendingSellerRequests = accountQueryRepository.countPendingSellerRequests();

        return UsersDTO.builder()
                .totalUsers(totalUsers)
                .customers(customers)
                .sellers(sellers)
                .admins(admins)
                .newThisMonth(newThisMonth)
                .activeUsers(activeUsers)
                .pendingSellerRequests(pendingSellerRequests)
                .build();
    }

    private ProductsDTO calculateProductsMetrics() {
        Long total = productQueryRepository.count();

        // FIX: Use countByStatusCode() with Integer parameter instead of countByStatus()
        Long active = productQueryRepository.countByStatus(ProductStatus.ACTIVE);
        Long pendingApproval = productQueryRepository.countByStatus(ProductStatus.PENDING);
        Long rejected = productQueryRepository.countByStatus(ProductStatus.REJECT);
        Long outOfStock = productQueryRepository.countOutOfStock();

        return ProductsDTO.builder()
                .total(total)
                .active(active)
                .pendingApproval(pendingApproval)
                .rejected(rejected)
                .outOfStock(outOfStock)
                .build();
    }

    private OrdersDTO calculateOrdersMetrics(LocalDateTime start, LocalDateTime end) {
        Long total = orderQueryRepository.countOrdersByDateRange(start, end);

        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("pending", orderQueryRepository.countByStatusAndDateRange(OrderStatus.PENDING, start, end));
        byStatus.put("confirmed", orderQueryRepository.countByStatusAndDateRange(OrderStatus.CONFIRMED, start, end));
        byStatus.put("paid", orderQueryRepository.countByStatusAndDateRange(OrderStatus.PAID, start, end));
        byStatus.put("cancelled", orderQueryRepository.countByStatusAndDateRange(OrderStatus.CANCELLED, start, end));

        Long cancelled = byStatus.get("cancelled");
        BigDecimal cancellationRate = total > 0
                ? BigDecimal.valueOf(cancelled).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return OrdersDTO.builder()
                .total(total)
                .byStatus(byStatus)
                .cancellationRate(cancellationRate)
                .build();
    }

    private InventoryDTO calculateInventoryMetrics() {
        BigDecimal totalValue = inventoryQueryRepository.calculateTotalInventoryValue();
        Integer totalQuantity = inventoryQueryRepository.calculateTotalQuantity();
        Long totalWarehouses = warehouseQueryRepository.countActiveWarehouses();
        Long lowStockProducts = inventoryQueryRepository.countLowStockProducts();
        Long outOfStockProducts = inventoryQueryRepository.countOutOfStockProducts();

        return InventoryDTO.builder()
                .totalValue(totalValue != null ? totalValue : BigDecimal.ZERO)
                .totalQuantity(totalQuantity != null ? totalQuantity : 0)
                .totalWarehouses(totalWarehouses)
                .lowStockProducts(lowStockProducts)
                .outOfStockProducts(outOfStockProducts)
                .build();
    }

    private List<RevenueChartDTO> calculateRevenueChart(LocalDateTime start, LocalDateTime end, String groupBy) {
        List<RevenueChartDTO> result = new ArrayList<>();

        // Validate and default groupBy
        if (groupBy == null || groupBy.trim().isEmpty()) {
            groupBy = "day";
        }
        groupBy = groupBy.toLowerCase();

        switch (groupBy) {
            case "day":
                return calculateDailyRevenue(start, end);
            case "week":
                return calculateWeeklyRevenue(start, end);
            case "month":
                return calculateMonthlyRevenue(start, end);
            default:
                log.warn("Invalid groupBy parameter: {}. Using 'day' as default.", groupBy);
                return calculateDailyRevenue(start, end);
        }
    }

    /**
     * Calculate revenue grouped by day
     */
    private List<RevenueChartDTO> calculateDailyRevenue(LocalDateTime start, LocalDateTime end) {
        List<RevenueChartDTO> result = new ArrayList<>();
        long days = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;

        for (int i = 0; i < days; i++) {
            LocalDate date = start.toLocalDate().plusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            RevenueChartDTO dto = calculateRevenueForPeriod(date, dayStart, dayEnd);
            result.add(dto);
        }

        return result;
    }

    /**
     * Calculate revenue grouped by week (Monday to Sunday)
     */
    private List<RevenueChartDTO> calculateWeeklyRevenue(LocalDateTime start, LocalDateTime end) {
        List<RevenueChartDTO> result = new ArrayList<>();

        // Find the first Monday on or before start date
        LocalDate currentDate = start.toLocalDate();
        while (currentDate.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
            currentDate = currentDate.minusDays(1);
        }

        LocalDate endDate = end.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            // Week starts on Monday
            LocalDate weekStart = currentDate;
            LocalDate weekEnd = currentDate.plusDays(6); // Sunday

            // Don't go beyond the requested end date
            if (weekEnd.isAfter(endDate)) {
                weekEnd = endDate;
            }

            LocalDateTime periodStart = weekStart.atStartOfDay();
            LocalDateTime periodEnd = weekEnd.atTime(LocalTime.MAX);

            RevenueChartDTO dto = calculateRevenueForPeriod(weekStart, periodStart, periodEnd);
            result.add(dto);

            // Move to next week
            currentDate = currentDate.plusWeeks(1);
        }

        return result;
    }

    /**
     * Calculate revenue grouped by month
     */
    private List<RevenueChartDTO> calculateMonthlyRevenue(LocalDateTime start, LocalDateTime end) {
        List<RevenueChartDTO> result = new ArrayList<>();

        // Start from the first day of the month
        LocalDate currentDate = start.toLocalDate().withDayOfMonth(1);
        LocalDate endDate = end.toLocalDate();

        while (!currentDate.isAfter(endDate)) {
            // Month period
            LocalDate monthStart = currentDate;
            LocalDate monthEnd = currentDate.withDayOfMonth(currentDate.lengthOfMonth());

            // Don't go beyond the requested end date
            if (monthEnd.isAfter(endDate)) {
                monthEnd = endDate;
            }

            LocalDateTime periodStart = monthStart.atStartOfDay();
            LocalDateTime periodEnd = monthEnd.atTime(LocalTime.MAX);

            RevenueChartDTO dto = calculateRevenueForPeriod(monthStart, periodStart, periodEnd);
            result.add(dto);

            // Move to next month
            currentDate = currentDate.plusMonths(1);
        }

        return result;
    }

    /**
     * Helper method to calculate revenue metrics for a given period
     */
    private RevenueChartDTO calculateRevenueForPeriod(LocalDate representativeDate,
                                                      LocalDateTime periodStart,
                                                      LocalDateTime periodEnd) {
        BigDecimal revenue = orderQueryRepository.calculateTotalRevenue(periodStart, periodEnd);
        Long orders = orderQueryRepository.countOrdersByDateRange(periodStart, periodEnd);
        BigDecimal profit = revenue != null ? revenue.multiply(new BigDecimal("0.30")) : BigDecimal.ZERO;
        Long customers = orderQueryRepository.countUniqueCustomers(periodStart, periodEnd);

        return RevenueChartDTO.builder()
                .date(representativeDate)
                .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                .orders(orders != null ? orders : 0L)
                .profit(profit)
                .customers(customers != null ? customers : 0L)
                .build();
    }

    private Map<String, PaymentMethodStatsDTO> calculatePaymentMethodStats(LocalDateTime start, LocalDateTime end) {
        List<Object[]> stats = paymentTransactionQueryRepository.calculatePaymentMethodStats(start, end);
        BigDecimal totalRevenue = orderQueryRepository.calculateTotalRevenue(start, end);

        Map<String, PaymentMethodStatsDTO> result = new HashMap<>();

        for (Object[] stat : stats) {
            String method = (String) stat[0];
            BigDecimal total = (BigDecimal) stat[1];
            Long orders = ((Number) stat[2]).longValue();

            BigDecimal percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? total.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            result.put(method, PaymentMethodStatsDTO.builder()
                    .total(total)
                    .percentage(percentage)
                    .orders(orders)
                    .build());
        }

        return result;
    }

    private List<CategoryPerformanceDTO> calculateCategoriesPerformance(LocalDateTime start, LocalDateTime end) {
        List<Object[]> stats = orderQueryRepository.calculateCategoryPerformance(start, end);
        BigDecimal totalRevenue = orderQueryRepository.calculateTotalRevenue(start, end);

        return stats.stream()
                .map(stat -> {
                    Long categoryId = ((Number) stat[0]).longValue();
                    String categoryName = (String) stat[1];
                    BigDecimal revenue = (BigDecimal) stat[2];
                    Long orders = ((Number) stat[3]).longValue();

                    BigDecimal percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                            ? revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO;

                    return CategoryPerformanceDTO.builder()
                            .categoryId(categoryId)
                            .categoryName(categoryName)
                            .revenue(revenue)
                            .orders(orders)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<RegionPerformanceDTO> calculateRegionsPerformance(LocalDateTime start, LocalDateTime end) {
        List<Object[]> stats = orderQueryRepository.calculateRegionPerformance(start, end);
        BigDecimal totalRevenue = orderQueryRepository.calculateTotalRevenue(start, end);

        return stats.stream()
                .map(stat -> {
                    String region = (String) stat[0];
                    BigDecimal revenue = (BigDecimal) stat[1];
                    Long orders = ((Number) stat[2]).longValue();

                    BigDecimal percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                            ? revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO;

                    return RegionPerformanceDTO.builder()
                            .region(region)
                            .revenue(revenue)
                            .orders(orders)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<UserGrowthDTO> calculateUserGrowthDay(LocalDateTime start, LocalDateTime end) {
        List<UserGrowthDTO> result = new ArrayList<>();
        long days = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;

        for (int i = 0; i < days; i++) {
            LocalDate date = start.toLocalDate().plusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            Long newUsers = userQueryRepository.countByCreatedAtBetween(dayStart, dayEnd);
            Long totalUsers = userQueryRepository.countByCreatedAtBefore(dayEnd);

            result.add(UserGrowthDTO.builder()
                    .date(date)
                    .newUsers(newUsers != null ? newUsers : 0)
                    .totalUsers(totalUsers != null ? totalUsers : 0)
                    .build());
        }

        return result;
    }

    private List<UserGrowthDTO> calculateUserGrowthMonth(LocalDateTime start, LocalDateTime end) {
        List<UserGrowthDTO> result = new ArrayList<>();

        // Lấy tháng bắt đầu và kết thúc
        YearMonth startMonth = YearMonth.from(start.toLocalDate());
        YearMonth endMonth = YearMonth.from(end.toLocalDate());

        long months = ChronoUnit.MONTHS.between(startMonth, endMonth) + 1;

        for (int i = 0; i < months; i++) {
            YearMonth month = startMonth.plusMonths(i);

            LocalDateTime monthStart = month.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = month.atEndOfMonth().atTime(LocalTime.MAX);

            Long newUsers = userQueryRepository.countByCreatedAtBetween(monthStart, monthEnd);
            Long totalUsers = userQueryRepository.countByCreatedAtBefore(monthEnd);

            result.add(
                    UserGrowthDTO.builder()
                            .date(month.atDay(1))
                            .newUsers(newUsers != null ? newUsers : 0)
                            .totalUsers(totalUsers != null ? totalUsers : 0)
                            .build()
            );
        }

        return result;
    }

    private Map<String, UserSegmentDTO> calculateUserSegments() {
        Map<String, UserSegmentDTO> segments = new HashMap<>();

        // VIP: total spent > 100M
        List<Object[]> vipStats = userQueryRepository.getUserSegmentStats(new BigDecimal("100000000"));
        if (!vipStats.isEmpty()) {
            Object[] stat = vipStats.get(0);
            segments.put("vip", UserSegmentDTO.builder()
                    .count(((Number) stat[0]).longValue())
                    .totalSpent((BigDecimal) stat[1])
                    .averageOrderValue((BigDecimal) stat[2])
                    .build());
        }

        // Regular: orders > 3
        List<Object[]> regularStats = userQueryRepository.getRegularUserStats(3);
        if (!regularStats.isEmpty()) {
            Object[] stat = regularStats.get(0);
            segments.put("regular", UserSegmentDTO.builder()
                    .count(((Number) stat[0]).longValue())
                    .totalSpent((BigDecimal) stat[1])
                    .averageOrderValue((BigDecimal) stat[2])
                    .build());
        }

        // New: orders < 3
        List<Object[]> newStats = userQueryRepository.getNewUserStats(3);
        if (!newStats.isEmpty()) {
            Object[] stat = newStats.get(0);
            segments.put("new", UserSegmentDTO.builder()
                    .count(((Number) stat[0]).longValue())
                    .totalSpent((BigDecimal) stat[1])
                    .averageOrderValue((BigDecimal) stat[2])
                    .build());
        }

        return segments;
    }
}