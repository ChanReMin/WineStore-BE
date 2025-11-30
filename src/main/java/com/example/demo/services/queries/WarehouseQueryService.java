package com.example.demo.services.queries;

import com.example.demo.dtos.responses.warehouse.WarehouseAdminStatisticsResponse;
import com.example.demo.dtos.responses.warehouse.WarehouseByCityResponse;
import com.example.demo.dtos.responses.warehouse.WarehouseDetailResponse;
import com.example.demo.dtos.responses.warehouse.WarehouseSellerStatisticsResponse;

import java.time.LocalDate;
import java.util.List;

public interface WarehouseQueryService {

    Object getWarehouses(Integer status, Long managerId, Integer page,
                         Integer limit, String search, String sortBy, String sortOrder);

    WarehouseDetailResponse getWarehouseDetailById(Long warehouseId);

    WarehouseSellerStatisticsResponse getSellerStatistics();

    WarehouseAdminStatisticsResponse getAdminStatistics(
            Long managerId, LocalDate fromDate, LocalDate toDate);
    List<String> getAllCities();
    WarehouseByCityResponse getWarehousesByCity(String city);

}
