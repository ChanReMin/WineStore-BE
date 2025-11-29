package com.example.demo.services.commands;

import com.example.demo.dtos.commands.product.AddPromotionsRequest;
import com.example.demo.dtos.responses.product.AddPromotionsResponse;

public interface ProductPromotionCommandService {

    AddPromotionsResponse addPromotionsToProduct(Long productId, AddPromotionsRequest request);

    void removePromotionFromProduct(Long productId, Long promotionId);
}
