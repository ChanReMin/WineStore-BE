package com.example.demo.controllers.seller;

import com.example.demo.dtos.commands.promotion.PromotionSearchRequest;
import com.example.demo.dtos.mappers.PromotionMapper;
import com.example.demo.dtos.responses.PaginationResponse;
import com.example.demo.dtos.responses.PromotionResponse;
import com.example.demo.dtos.responses.SellerPromotionListResponse;
import com.example.demo.services.queries.PromotionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerPromotionController {

    private final PromotionQueryService promotionQueryService;

    @GetMapping("/promotions")
    @PreAuthorize("hasRole('SELLER')") // Assuming a 'SELLER' role exists
    public ResponseEntity<SellerPromotionListResponse> getSellerPromotions(PromotionSearchRequest request) {
        Page<com.example.demo.entities.Promotion> promotionsPage = promotionQueryService.getSellerPromotions(
                java.util.Optional.ofNullable(request.getPage()),
                java.util.Optional.ofNullable(request.getLimit()),
                java.util.Optional.ofNullable(request.getStatus()),
                java.util.Optional.ofNullable(request.getSearch()),
                java.util.Optional.ofNullable(request.getSortBy()),
                java.util.Optional.ofNullable(request.getSortOrder())
        );

        List<PromotionResponse> promotionResponses = promotionsPage.getContent().stream()
                .map(PromotionMapper::toResponse)
                .collect(Collectors.toList());

        PaginationResponse pagination = PaginationResponse.builder()
                .currentPage(promotionsPage.getNumber() + 1)
                .totalPages(promotionsPage.getTotalPages())
                .totalItems(promotionsPage.getTotalElements())
                .perPage(promotionsPage.getSize())
                .build();

        SellerPromotionListResponse.Data data = SellerPromotionListResponse.Data.builder()
                .promotions(promotionResponses)
                .pagination(pagination)
                .build();

        return ResponseEntity.ok(SellerPromotionListResponse.builder()
                .success(true)
                .data(data)
                .build());
    }
}
