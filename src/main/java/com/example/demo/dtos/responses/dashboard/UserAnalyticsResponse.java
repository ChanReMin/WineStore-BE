package com.example.demo.dtos.responses.dashboard;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalyticsResponse {
    private List<UserGrowthDTO> userGrowth;

    private Map<String, UserSegmentDTO> userSegments;
}