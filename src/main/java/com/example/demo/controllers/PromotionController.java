package com.example.demo.controllers;

import com.example.demo.dtos.commands.promotion.CreatePromotionRequest;
import com.example.demo.dtos.commands.promotion.ExportPromotionStatisticsRequest;
import com.example.demo.dtos.commands.promotion.TogglePromotionStatusRequest; // Import TogglePromotionStatusRequest
import com.example.demo.dtos.commands.promotion.UpdatePromotionRequest;
import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.promotion.CreatePromotionResponse;
import com.example.demo.dtos.responses.promotion.ExportPromotionStatisticsResponse;
import com.example.demo.dtos.responses.promotion.PromotionDetailResponse;
import com.example.demo.dtos.responses.promotion.PromotionListResponse;
import com.example.demo.dtos.responses.promotion.PromotionStatisticsResponse; // Import PromotionStatisticsResponse
import com.example.demo.dtos.responses.promotion.TogglePromotionStatusResponse; // Import TogglePromotionStatusResponse
import com.example.demo.dtos.responses.promotion.UpdatePromotionResponse;
import com.example.demo.services.commands.ServiceCommandImpl.PromotionCommandServiceImpl;
import com.example.demo.services.queries.serviceQueryImpl.PromotionQueryServiceImpl;
import jakarta.validation.Valid; // Import Valid annotation
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionQueryServiceImpl promotionQueryService;
    private final PromotionCommandServiceImpl promotionCommandService;

    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SuccessResponse<PromotionListResponse>> getPromotions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "created_at") String sort_by,
            @RequestParam(required = false, defaultValue = "desc") String sort_order
    ) {
        PromotionListResponse data = promotionQueryService.getPromotions(page, limit, status, search, sort_by, sort_order);

        SuccessResponse<PromotionListResponse> response = SuccessResponse.<PromotionListResponse>builder()
                .success(true)
                .data(data)
                .message("Promotions retrieved successfully.")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{promotion_id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SuccessResponse<PromotionDetailResponse>> getPromotionDetails(
            @PathVariable("promotion_id") Long promotionId
    ) {
        PromotionDetailResponse data = promotionQueryService.getPromotionDetails(promotionId);

        SuccessResponse<PromotionDetailResponse> response = SuccessResponse.<PromotionDetailResponse>builder()
                .success(true)
                .data(data)
                .message("Promotion details retrieved successfully.")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SuccessResponse<CreatePromotionResponse>> createPromotion(
            @Valid @RequestBody CreatePromotionRequest request // Add @Valid annotation
    ) {
        CreatePromotionResponse data = promotionCommandService.createPromotion(request);

        SuccessResponse<CreatePromotionResponse> response = SuccessResponse.<CreatePromotionResponse>builder()
                .success(true)
                .data(data)
                .message("Promotion created successfully")
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{promotion_id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SuccessResponse<UpdatePromotionResponse>> updatePromotion(
            @PathVariable("promotion_id") Long promotionId,
            @Valid @RequestBody UpdatePromotionRequest request // Add @Valid annotation
    ) {
        UpdatePromotionResponse data = promotionCommandService.updatePromotion(promotionId, request);

        SuccessResponse<UpdatePromotionResponse> response = SuccessResponse.<UpdatePromotionResponse>builder()
                .success(true)
                .data(data)
                .message("Promotion updated successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{promotion_id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SuccessResponse<Void>> deletePromotion(
            @PathVariable("promotion_id") Long promotionId
    ) {
        promotionCommandService.deletePromotion(promotionId);

        SuccessResponse<Void> response = SuccessResponse.<Void>builder()
                .success(true)
                .message("Promotion deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{promotion_id}/toggle")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SuccessResponse<TogglePromotionStatusResponse>> togglePromotionStatus(
            @PathVariable("promotion_id") Long promotionId,
            @Valid @RequestBody TogglePromotionStatusRequest request // Add @Valid annotation
    ) {
        TogglePromotionStatusResponse data = promotionCommandService.togglePromotionStatus(promotionId, request);

        SuccessResponse<TogglePromotionStatusResponse> response = SuccessResponse.<TogglePromotionStatusResponse>builder()
                .success(true)
                .data(data)
                .message("Update status successful")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{promotion_id}/export")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SuccessResponse<ExportPromotionStatisticsResponse>> exportPromotionStatistics(
            @PathVariable("promotion_id") Long promotionId,
            @Valid @RequestBody ExportPromotionStatisticsRequest request
    ) {
        ExportPromotionStatisticsResponse data = promotionCommandService.exportPromotionStatistics(promotionId, request);

        SuccessResponse<ExportPromotionStatisticsResponse> response = SuccessResponse.<ExportPromotionStatisticsResponse>builder()
                .success(true)
                .data(data)
                .message("File is being created")
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{promotion_id}/statistics")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SuccessResponse<PromotionStatisticsResponse>> getPromotionStatistics(
            @PathVariable("promotion_id") Long promotionId
    ) {
        PromotionStatisticsResponse data = promotionQueryService.getPromotionStatistics(promotionId);

        SuccessResponse<PromotionStatisticsResponse> response = SuccessResponse.<PromotionStatisticsResponse>builder()
                .success(true)
                .data(data)
                .message("Promotion statistics retrieved successfully.")
                .build();
        return ResponseEntity.ok(response);
    }
}
