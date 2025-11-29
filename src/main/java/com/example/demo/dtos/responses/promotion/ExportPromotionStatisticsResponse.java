package com.example.demo.dtos.responses.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportPromotionStatisticsResponse {
    private String export_id;
    private String status; // e.g., "processing", "completed", "failed"
    private String download_url;// URL to download the file once completed
}
