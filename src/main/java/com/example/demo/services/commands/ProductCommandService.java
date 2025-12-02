package com.example.demo.services.commands;

import com.example.demo.dtos.commands.product.CreateProductRequest;
import com.example.demo.dtos.commands.product.UpdateProductRequest;
import com.example.demo.dtos.commands.product.UpdateProductStatusRequest;
import com.example.demo.dtos.responses.product.UpdateProductStatusResponse;
import com.example.demo.dtos.responses.product.WriteProductResponse;

public interface ProductCommandService {

    WriteProductResponse createProduct(CreateProductRequest request);

    WriteProductResponse updateProduct(Long id, UpdateProductRequest request);

    void deleteProduct(Long id);

    UpdateProductStatusResponse updateProductStatus(Long productId, UpdateProductStatusRequest request);
}
