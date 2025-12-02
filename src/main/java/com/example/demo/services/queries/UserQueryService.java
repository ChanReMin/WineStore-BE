package com.example.demo.services.queries;

import com.example.demo.dtos.commands.user.ExportUsersRequest;
import com.example.demo.dtos.responses.user.*;

import java.time.LocalDate;

public interface UserQueryService {
    UserListResponse getUsersList(
            Integer page,
            Integer limit,
            String search,
            String role,
            String status,
            String sortBy,
            String sortOrder
    );
    UserDetailResponse getUserDetail(Long userId);
    UserStatisticsResponse getUserStatistics(LocalDate startDate, LocalDate endDate);
    ExportUsersResponse exportUsers(ExportUsersRequest request);
}
