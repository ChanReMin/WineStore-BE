package com.example.demo.dtos.responses.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersDTO {
    private Long totalUsers;

    private Long customers;
    private Long sellers;
    private Long admins;

    private Long newThisMonth;

    private Long activeUsers;

    private Long pendingSellerRequests;
}

