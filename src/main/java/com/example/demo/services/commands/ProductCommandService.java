package com.example.demo.services.commands;

import com.example.demo.dtos.commands.product.UpdateProductStatusRequest;
import com.example.demo.dtos.commands.product.WriteProductRequest;
import com.example.demo.dtos.responses.product.UpdateProductStatusResponse;
import com.example.demo.dtos.responses.product.WriteProductResponse;

public interface ProductCommandService {

    WriteProductResponse createProduct(WriteProductRequest request);

    WriteProductResponse updateProduct(Long id, WriteProductRequest request);

    void deleteProduct(Long id);

    UpdateProductStatusResponse updateProductStatus(Long productId, UpdateProductStatusRequest request);
}
